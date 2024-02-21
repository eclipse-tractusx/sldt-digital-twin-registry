/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.eclipse.tractusx.semantics.registry.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.accesscontrol.api.AccessControlRuleService;
import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityContext;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityCriteria;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.registry.model.ReferenceKeyType;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal;
import org.eclipse.tractusx.semantics.registry.utils.ShellCursor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GranularShellAccessHandler implements ShellAccessHandler {
   private final String owningTenantId;

   private final AccessControlRuleService accessControlRuleService;

   public GranularShellAccessHandler( final RegistryProperties registryProperties, final AccessControlRuleService accessControlRuleService ) {
      this.owningTenantId = registryProperties.getIdm().getOwningTenantId();
      this.accessControlRuleService = accessControlRuleService;
   }

   @Override
   public boolean supportsGranularAccessControl() {
      return true;
   }

   @Override
   public Specification<Shell> shellFilterSpecification( String sortFieldName, ShellCursor cursor, String externalSubjectId ) {
      return ( root, query, criteriaBuilder ) -> {
         Instant searchValue = cursor.getShellSearchCursor();
         query.orderBy( criteriaBuilder.asc( criteriaBuilder.coalesce( root.get( sortFieldName ), Instant.now() ) ) );
         return criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue );
      };
   }

   @Override
   public List<String> filterToVisibleShellIdsForLookup( Set<SpecificAssetId> userQuery, List<ShellIdentifierMinimal> shellIdentifiers,
         String externalSubjectId )
         throws DenyAccessException {
      List<String> idsInTheExistingOrder = shellIdentifiers.stream()
            .map( ShellIdentifierMinimal::shellId )
            .distinct()
            .toList();
      if ( owningTenantId.equals( externalSubjectId ) ) {
         return idsInTheExistingOrder;
      }

      List<ShellVisibilityContext> shellContexts = shellIdentifiers.stream()
            .collect( Collectors.groupingBy( ShellIdentifierMinimal::shellId ) ).entrySet().stream()
            .map( entry -> new ShellVisibilityContext( entry.getKey(), entry.getValue().stream()
                  .map( shellIdentifier -> new SpecificAssetId( shellIdentifier.namespace(), shellIdentifier.identifier() ) )
                  .collect( Collectors.toSet() ) ) )
            .toList();
      List<String> allVisible = accessControlRuleService.filterValidSpecificAssetIdsForLookup( userQuery, shellContexts, externalSubjectId );
      return idsInTheExistingOrder.stream()
            .filter( allVisible::contains )
            .toList();
   }

   /**
    * This method filters out the shell-properties based on externalSubjectId in the specificAssetIds.<br>
    * 1. Condition: The owner of the shell has full access to the shell.<br>
    * 2. Condition: If there is an access rule giving access to the @param externalSubjectId to see a specificAssetId it will be visible<br>
    * 3. Condition: If there is an access rule giving access to the @param externalSubjectId to see a submodel it will be visible<br>
    * 4. Condition: If only PUBLIC_READABLE access rules apply, the shell content will be filtered even further
    *
    * @param shell
    * @param externalSubjectId externalSubjectId/tenantId
    * @return filtered Shell
    */
   @Override
   @Nullable
   public Shell filterShellProperties( Shell shell, String externalSubjectId ) {
      if ( owningTenantId.equals( externalSubjectId ) ) {
         return shell;
      }

      try {
         ShellVisibilityContext shellContext = toShellVisibilityContext( shell );
         ShellVisibilityCriteria visibilityCriteria = accessControlRuleService.fetchVisibilityCriteriaForShell( shellContext, externalSubjectId );
         return filterShellContents( shell, visibilityCriteria );
      } catch ( DenyAccessException e ) {
         if ( log.isDebugEnabled() ) {
            log.debug( "Filtering out shell: {} for externalSubjectId: {} as access is denied.", shell.getId(), externalSubjectId );
         }
         return null;
      }
   }

   @Override
   public List<Shell> filterListOfShellProperties( List<Shell> shells, String externalSubjectId ) {
      if ( owningTenantId.equals( externalSubjectId ) ) {
         return shells;
      }

      final var visibilityContexts = shells.stream()
            .map( this::toShellVisibilityContext )
            .toList();
      final var visibilityCriteria = accessControlRuleService.fetchVisibilityCriteriaForShells( visibilityContexts, externalSubjectId );
      if ( visibilityCriteria.isEmpty() ) {
         return Collections.emptyList();
      }
      return shells.stream()
            .map( shell -> filterShellContents( shell, visibilityCriteria.get( shell.getIdExternal() ) ) )
            .filter( Objects::nonNull )
            .toList();
   }

   private Shell filterShellContents( Shell shell, ShellVisibilityCriteria visibilityCriteria ) {
      if ( visibilityCriteria == null ) {
         return null;
      }
      Set<ShellIdentifier> filteredIdentifiers = filterSpecificAssetIdsByTenantId( shell.getIdentifiers(), visibilityCriteria );
      Set<Submodel> filteredSubmodels = shell.getSubmodels().stream()
            .filter( submodel -> submodel.getSemanticId().getKeys().stream()
                  .anyMatch( key -> key.getType() == ReferenceKeyType.SUBMODEL
                                    && visibilityCriteria.visibleSemanticIds().contains( key.getValue() ) ) )
            .collect( Collectors.toSet() );
      final Shell filtered;
      if ( visibilityCriteria.publicOnly() ) {
         // Filter out globalAssetId from specificAssetId. TODO: implement to save globalAssetId in separate database column
         // GlobalAssetId is set via mapper. In case of only read access, no globalAssetId should be shown.
         Set<ShellIdentifier> filteredIdentifiersWithNoGlobalAssetId = filteredIdentifiers.stream().filter(
                     shellIdentifier -> !shellIdentifier.getKey().equals( ShellIdentifier.GLOBAL_ASSET_ID_KEY ) )
               .collect( Collectors.toSet() );
         filtered = new Shell()
               .withIdentifiers( filteredIdentifiersWithNoGlobalAssetId )
               .withSubmodels( filteredSubmodels )
               .withIdExternal( shell.getIdExternal() )
               .withId( shell.getId() )
               .withCreatedDate( shell.getCreatedDate() );
      } else {
         filtered = shell.withIdentifiers( filteredIdentifiers ).withSubmodels( filteredSubmodels );
      }
      return filtered;
   }

   private ShellVisibilityContext toShellVisibilityContext( Shell shell ) {
      Set<SpecificAssetId> specificAssetIds = shell.getIdentifiers().stream()
            .map( id -> new SpecificAssetId( id.getKey(), id.getValue() ) )
            .collect( Collectors.toSet() );
      return new ShellVisibilityContext( shell.getIdExternal(), specificAssetIds );
   }

   private Set<ShellIdentifier> filterSpecificAssetIdsByTenantId( Set<ShellIdentifier> shellIdentifiers, ShellVisibilityCriteria visibilityCriteria ) {
      //noinspection SimplifyStreamApiCallChains
      return shellIdentifiers.stream()
            .filter( identifier -> identifier.getKey().equals( ShellIdentifier.GLOBAL_ASSET_ID_KEY )
                                   || visibilityCriteria.visibleSpecificAssetIdNames().contains( identifier.getKey() ) )
            //TODO: Do we need to clear the list of external subject Ids?
            .map( identifier -> {
               Optional.ofNullable( identifier.getExternalSubjectId() )
                     .ifPresent( extSubId -> extSubId.setKeys( Collections.emptySet() ) );
               return identifier;
            } )
            .collect( Collectors.toSet() );

   }
}
