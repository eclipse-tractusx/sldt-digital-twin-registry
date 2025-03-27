/*******************************************************************************
 * Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.semantics.registry.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetLink;
import org.eclipse.tractusx.semantics.aas.registry.model.GetAssetAdministrationShellDescriptorsResult;
import org.eclipse.tractusx.semantics.aas.registry.model.LangStringTextType;
import org.eclipse.tractusx.semantics.aas.registry.model.Reference;
import org.eclipse.tractusx.semantics.aas.registry.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.registry.dto.ShellCollectionDto;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellDescription;
import org.eclipse.tractusx.semantics.registry.model.ShellDisplayName;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReference;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSemanticReference;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSupplemSemanticReference;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper( uses = {
      SubmodelMapper.class }, componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE )
public interface ShellMapper {
   @Mappings( {
         @Mapping( target = "idExternal", source = "id" ),
         @Mapping( target = "identifiers", source = "specificAssetIds" ),
         @Mapping( target = "descriptions", source = "description" ),
         @Mapping( target = "submodels", source = "submodelDescriptors" ),
         @Mapping( target = "shellType", source = "assetType" ),
         @Mapping( target = "shellKind", source = "assetKind" ),
         @Mapping( target = "id", ignore = true ),
         @Mapping( target = "displayNames", source = "displayName" ),
   } )
   Shell fromApiDto( AssetAdministrationShellDescriptor apiDto );

   default Instant map( final OffsetDateTime value ) {
      return Optional.ofNullable( value ).map( OffsetDateTime::toInstant ).orElse( null );
   }

   default OffsetDateTime map( final Instant value ) {
      return Optional.ofNullable( value ).map( instant -> instant.atOffset( ZoneId.systemDefault().getRules().getOffset( instant ) ) ).orElse( null );
   }

   ShellDescription mapShellDescription( LangStringTextType description );

   ShellDisplayName mapShellDisplayName( LangStringTextType displayName );

   @Mappings( {
         @Mapping( target = "key", source = "name" ),
         @Mapping( target = "supplementalSemanticIds", source = "supplementalSemanticIds" ),
         @Mapping( target = "semanticId", source = "semanticId" ),
         @Mapping( target = "externalSubjectId", source = "externalSubjectId" ),
   } )
   ShellIdentifier fromApiDto( SpecificAssetId apiDto );

   @Mappings( {
         @Mapping( target = "key", source = "name" ),
   } )
   ShellIdentifier fromApiDto( AssetLink apiDto );

   Set<ShellIdentifier> fromAssetLinkApiDto( List<AssetLink> apiDto );

   ShellIdentifierSupplemSemanticReference maptoShellIdentifierSupplemSemanticReference( Reference supplementalSemanticId );

   ShellIdentifierSemanticReference maptoShellIdentifierSemanticReference( Reference semanticId );

   ShellIdentifierExternalSubjectReference maptoShellIdentifierExternalSubjectReference( Reference externalSubjectId );

   Set<ShellIdentifier> fromApiDto( List<SpecificAssetId> apiDto );

   @Mappings( {
         @Mapping( target = "name", source = "key" ),
   } )
   SpecificAssetId fromDtoApi( ShellIdentifier apiDto );

   @Mappings( {
         @Mapping( source = "idExternal", target = "id" ),
         @Mapping( source = "identifiers", target = "specificAssetIds" ),
         @Mapping( source = "descriptions", target = "description" ),
         @Mapping( source = "submodels", target = "submodelDescriptors" ),
         @Mapping( source = "displayNames", target = "displayName" ),
   } )
   @InheritInverseConfiguration
   AssetAdministrationShellDescriptor toApiDto( Shell shell );

   LangStringTextType mapAssetDescription( ShellDescription description );

   LangStringTextType mapAssetDisplayName( ShellDisplayName shellDisplayName );

   @Mappings( {
         @Mapping( source = "items", target = "result" ),
         @Mapping( source = "cursor", target = "pagingMetadata.cursor" ),
   } )
   GetAssetAdministrationShellDescriptorsResult toApiDto( ShellCollectionDto shell );

   List<SpecificAssetId> toApiDto( Set<ShellIdentifier> shell );

   @AfterMapping
   default Shell convertGlobalAssetIdToShellIdentifier( final AssetAdministrationShellDescriptor apiDto, @MappingTarget final Shell shell ) {
      return ShellMapperCustomization.globalAssetIdToShellIdentifier( apiDto, shell );
   }

   @AfterMapping
   default void convertShellIdentifierToGlobalAssetId( final Shell shell, @MappingTarget final AssetAdministrationShellDescriptor apiDto ) {
      ShellMapperCustomization.shellIdentifierToGlobalAssetId( shell, apiDto );
   }

   @AfterMapping
   default void removeGlobalAssetIdFromIdentifiers( @MappingTarget final List<SpecificAssetId> apiDto ) {
      ShellMapperCustomization.removeGlobalAssetIdIdentifier( apiDto );
   }
}
