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

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReferenceKey;
import org.eclipse.tractusx.semantics.registry.utils.ShellCursor;
import org.eclipse.tractusx.semantics.registry.utils.ShellSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultShellAccessHandler implements ShellAccessHandler {
   private final String owningTenantId;
   private final String externalSubjectIdWildcardPrefix;
   private final List<String> externalSubjectIdWildcardAllowedTypes;

   public DefaultShellAccessHandler( RegistryProperties registryProperties ) {
      this.owningTenantId = registryProperties.getIdm().getOwningTenantId();
      this.externalSubjectIdWildcardPrefix = registryProperties.getExternalSubjectIdWildcardPrefix();
      this.externalSubjectIdWildcardAllowedTypes = registryProperties.getExternalSubjectIdWildcardAllowedTypes();
   }

   @Override
   public Specification<Shell> shellFilterSpecification( final String sortFieldName, final ShellCursor cursor, final String externalSubjectId,
         final OffsetDateTime createdAfter ) {
      return new ShellSpecification<>( sortFieldName, cursor, externalSubjectId, owningTenantId, externalSubjectIdWildcardPrefix,
            externalSubjectIdWildcardAllowedTypes, createdAfter );
   }

   /**
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

      Set<ShellIdentifier> filteredIdentifiers = filterSpecificAssetIdsByTenantId( shell.getIdentifiers(), externalSubjectId );
      boolean hasOnlyPublicAccess = filteredIdentifiers.stream().noneMatch( shellIdentifier -> {
               if ( shellIdentifier.getExternalSubjectId() == null ) {
                  return false;
               }
               return shellIdentifier.getExternalSubjectId().getKeys().stream().anyMatch( key -> key.getValue().equals( externalSubjectId ) );
            }
      );

      if ( hasOnlyPublicAccess ) {
         // Filter out globalAssetId from specificAssetId. TODO: implement to save globalAssetId in separate database column
         // GlobalAssetId is set via mapper. In case of only read access, no globalAssetId should be shown.
         Set<ShellIdentifier> filteredIdentifiersWithNoGlobalAssetId = filteredIdentifiers.stream().filter(
                     shellIdentifier -> !shellIdentifier.getKey().equals( ShellIdentifier.GLOBAL_ASSET_ID_KEY ) )
               .collect( Collectors.toSet() );
         return new Shell()
               .withIdentifiers( filteredIdentifiersWithNoGlobalAssetId )
               .withSubmodels( shell.getSubmodels() )
               .withIdExternal( shell.getIdExternal() )
               .withId( shell.getId() )
               .withCreatedDate( shell.getCreatedDate() );
      }
      return shell.withIdentifiers( filteredIdentifiers );
   }

   @Override
   public List<Shell> filterListOfShellProperties( List<Shell> shells, String externalSubjectId ) {
      return shells.stream()
            .map( shell -> filterShellProperties( shell, externalSubjectId ) )
            .toList();
   }

   private Set<ShellIdentifier> filterSpecificAssetIdsByTenantId( Set<ShellIdentifier> shellIdentifiers, String tenantId ) {
      // the owning tenant should always see all identifiers
      if ( tenantId.equals( owningTenantId ) ) {
         return shellIdentifiers;
      }

      Set<ShellIdentifier> externalSubjectIdSet = new HashSet<>();
      for ( ShellIdentifier identifier : shellIdentifiers ) {
         // Check if specificAssetId is globalAssetId -> TODO: implement to save globalAssetId in separate database column
         if ( identifier.getKey().equals( ShellIdentifier.GLOBAL_ASSET_ID_KEY ) ) {
            externalSubjectIdSet.add( identifier );
         }
         if ( identifier.getExternalSubjectId() != null ) {
            Set<ShellIdentifierExternalSubjectReferenceKey> optionalReferenceKey =
                  identifier.getExternalSubjectId().getKeys().stream().filter( shellIdentifierExternalSubjectReferenceKey ->
                        // Match if externalSubjectId = tenantId
                              shellIdentifierExternalSubjectReferenceKey.getValue().equals( tenantId )
                        // or match if externalSubjectId = externalSubjectIdWildcardPrefix and key of identifier (for example manufacturerPartId) is allowing wildcard.
                              || (shellIdentifierExternalSubjectReferenceKey.getValue().equals( externalSubjectIdWildcardPrefix )
                                  && externalSubjectIdWildcardAllowedTypes.contains( identifier.getKey() )) )
                        .collect( Collectors.toSet() );
            if ( !optionalReferenceKey.isEmpty() ) {
               identifier.getExternalSubjectId().setKeys( optionalReferenceKey );
               externalSubjectIdSet.add( identifier );
            }
         }
      }
      return externalSubjectIdSet;
   }
}