/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
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
 *
 ******************************************************************************/

package org.eclipse.tractusx.semantics.registry.service;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.accesscontrol.api.AccessControlRuleService;
import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityCriteria;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.registry.model.ReferenceKeyType;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
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
      //TODO: must be reviewed, not sure this will be good
      return ( root, query, criteriaBuilder ) -> {
         Instant searchValue = cursor.getShellSearchCursor();
         query.orderBy( criteriaBuilder.asc( criteriaBuilder.coalesce( root.get( sortFieldName ), Instant.now() ) ) );
         return criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue );
      };
   }

   @Override
   public Set<ShellIdentifier> filterShellIdsForLookup( Set<ShellIdentifier> shellIdentifiers, String externalSubjectId ) throws DenyAccessException {
      if ( externalSubjectId.equals( owningTenantId ) ) {
         return shellIdentifiers;
      }
      Map<SpecificAssetId, ShellIdentifier> specificAssetIds = shellIdentifiers.stream()
            .collect( Collectors.toMap( id -> new SpecificAssetId( id.getKey(), id.getValue() ), Function.identity() ) );
      return accessControlRuleService.filterValidSpecificAssetIdsForLookup( specificAssetIds.keySet(), externalSubjectId ).stream()
            .map( specificAssetIds::get )
            .collect( Collectors.toSet() );
   }

   /**
    * TODO: update documentation
    * This method filter out the shell-properties based on externalSubjectId in the specificAssetIds.<br>
    * 1. Condition: The owner of the shell has full access to the shell.<br>
    * 2. Condition: If the given @param externalSubjectId is included in one of the specificAssetIds, all shell-properties are visible. Only the list of specificAssetIds are limited to given externalSubjectId.<br>
    * 3. Condition: If the given @param externalSubjectId is not included in one of the specificAssetIds, only few properties are visible:idShort, submodelDescriptors
    *
    * @param shell
    * @param externalSubjectId externalSubjectId/tenantId
    * @return filtered Shell
    */
   //Could map to null if the shell should not be visible at all
   @Override
   @Nullable
   public Shell filterShellProperties( Shell shell, String externalSubjectId ) {
      if ( externalSubjectId.equals( owningTenantId ) ) {
         return shell;
      }

      try {
         Set<SpecificAssetId> specificAssetIds = shell.getIdentifiers().stream()
               .map( id -> new SpecificAssetId( id.getKey(), id.getValue() ) )
               .collect( Collectors.toSet() );
         ShellVisibilityCriteria visibilityCriteria = accessControlRuleService.fetchVisibilityCriteriaForShell( specificAssetIds, externalSubjectId );
         Set<ShellIdentifier> filteredIdentifiers = filterSpecificAssetIdsByTenantId( shell.getIdentifiers(), visibilityCriteria );
         Set<Submodel> filteredSubmodels = shell.getSubmodels().stream()
               .filter( submodel -> submodel.getSemanticId().getKeys().stream()
                     .anyMatch( key -> key.getType() == ReferenceKeyType.SUBMODEL
                                       //TODO: is it okay to use full text matching for the semantic Id comparisons???
                                       && visibilityCriteria.visibleSemanticIds().contains( key.getValue() ) ) )
               .collect( Collectors.toSet() );
         return shell.withIdentifiers( filteredIdentifiers ).withSubmodels( filteredSubmodels );
      } catch ( DenyAccessException e ) {
         if ( log.isDebugEnabled() ) {
            log.debug( "Filtering out shell: {} for externalSubjectId: {} as access is denied.", shell.getId(), externalSubjectId );
         }
         return null;
      }
   }

   private Set<ShellIdentifier> filterSpecificAssetIdsByTenantId( Set<ShellIdentifier> shellIdentifiers, ShellVisibilityCriteria visibilityCriteria ) {
      //noinspection SimplifyStreamApiCallChains
      return shellIdentifiers.stream()
            .filter( identifier -> identifier.getKey().equals( ShellIdentifier.GLOBAL_ASSET_ID_KEY )
                                   || visibilityCriteria.visibleSpecificAssetIdNames().contains( identifier.getKey() ) )
            //TODO: Do we need to clear the list of external subject Ids?
            .map( identifier -> {
               identifier.getExternalSubjectId().setKeys( Collections.emptySet() );
               return identifier;
            } )
            .collect( Collectors.toSet() );

   }
}
