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

package org.eclipse.tractusx.semantics.registry.service;

import static org.springframework.data.domain.PageRequest.ofSize;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.aas.registry.model.GetAllAssetAdministrationShellIdsByAssetLink200Response;
import org.eclipse.tractusx.semantics.aas.registry.model.PagedResultPagingMetadata;
import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.registry.dto.BatchResultDto;
import org.eclipse.tractusx.semantics.registry.dto.ShellCollectionDto;
import org.eclipse.tractusx.semantics.registry.dto.SubmodelCollectionDto;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellMinimal;
import org.eclipse.tractusx.semantics.registry.repository.ShellIdentifierRepository;
import org.eclipse.tractusx.semantics.registry.repository.ShellRepository;
import org.eclipse.tractusx.semantics.registry.repository.SubmodelRepository;
import org.eclipse.tractusx.semantics.registry.utils.ShellCursor;
import org.eclipse.tractusx.semantics.registry.utils.ShellSpecification;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShellService {

   public static final String DUPLICATE_SUBMODEL_EXCEPTION = "An AssetAdministrationSubmodel for the given identification does already exists.";
   public static final String DUPLICATE_SUBMODEL_ID_SHORT_EXCEPTION = "An AssetAdministration Submodel for the given IdShort does already exists.";
   private static final String SORT_FIELD_NAME_SHELL = "createdDate";
   private static final String SORT_FIELD_NAME_SUBMODEL = "id";
   private static final int MAXIMUM_RECORDS = 1000;

   private final ShellRepository shellRepository;
   private final ShellIdentifierRepository shellIdentifierRepository;
   private final SubmodelRepository submodelRepository;
   private final ShellAccessHandler shellAccessHandler;
   private final String owningTenantId;
   private final String externalSubjectIdWildcardPrefix;
   private final List<String> externalSubjectIdWildcardAllowedTypes;

   public ShellService( ShellRepository shellRepository,
         ShellIdentifierRepository shellIdentifierRepository,
         SubmodelRepository submodelRepository,
         RegistryProperties registryProperties,
         ShellAccessHandler shellAccessHandler ) {
      this.shellRepository = shellRepository;
      this.shellIdentifierRepository = shellIdentifierRepository;
      this.submodelRepository = submodelRepository;
      this.shellAccessHandler = shellAccessHandler;
      this.owningTenantId = registryProperties.getIdm().getOwningTenantId();
      this.externalSubjectIdWildcardPrefix = registryProperties.getExternalSubjectIdWildcardPrefix();
      this.externalSubjectIdWildcardAllowedTypes = registryProperties.getExternalSubjectIdWildcardAllowedTypes();
   }

   @Transactional
   public Shell save( Shell shell ) {
      if ( shellRepository.findByIdExternal( shell.getIdExternal() ).isPresent() ) {
         throw new DuplicateKeyException( "An AssetAdministrationShell for the given identification does already exists." );
      }

      validateIdShort( shell );

      return shellRepository.save( shell );
   }

   /**
    * Checks IdShort in shell level against DB & validate duplicate IdShort values in Submodels
    * @param shell
    */
   private void validateIdShort( Shell shell ) {
      //Check uniqueness of IdShort in shell level
      Optional.ofNullable( shell.getIdShort() ).map( shellRepository::existsByIdShort ).filter( BooleanUtils::isTrue )
            .ifPresent( aBoolean -> {
               throw new DuplicateKeyException( "An AssetAdministrationShell for the given IdShort already exists." );
            } );

      checkForDuplicateIdShortWithInSubModels( shell );
   }

   private void checkForDuplicateIdShortWithInSubModels( Shell shell ) {
      //Check uniqueness of IdShort in Sub-model level
      List<String> idShortList = Optional.of( shell ).map( Shell::getSubmodels ).stream()
            .flatMap( Collection::stream )
            .map( Submodel::getIdShort )
            .filter( StringUtils::isNotBlank )
            .map( String::toLowerCase )
            .toList();

      boolean isDuplicateIdShortPresent = Optional.of( idShortList ).filter( idShorts -> idShortList.stream().distinct().count() != idShorts.size() )
            .isPresent();

      if ( isDuplicateIdShortPresent ) {
         throw new DuplicateKeyException( DUPLICATE_SUBMODEL_ID_SHORT_EXCEPTION );
      }

   }

   public void mapShellCollection( Shell shell ) {
      shell.getIdentifiers().forEach( shellIdentifier -> shellIdentifier.setShellId( shell ) );
      shell.getSubmodels().forEach( submodel -> submodel.setShellId( shell ) );
      shell.getDescriptions().forEach( description -> description.setShellId( shell ) );
      shell.getDisplayNames().forEach( description -> description.setShellId( shell ) );
      mapShellIdentifier( shell.getIdentifiers().stream() );
   }

   public void mapSubmodel( Set<Submodel> submodels ) {
      submodels.forEach( submodel -> submodel.getEndpoints()
            .forEach( submodelEndpoint -> submodelEndpoint.getSubmodelSecurityAttribute().forEach( submodelSecurityAttribute -> {
               submodelSecurityAttribute.setSubmodelEndpoint( submodelEndpoint );
            } ) ) );

      submodels.forEach( submodel -> {
         if ( submodel.getSemanticId() != null ) {
            submodel.getSemanticId().getKeys().stream().filter( Objects::nonNull ).forEach( key -> {
               key.setSubmodelSemanticIdReference( submodel.getSemanticId() );
            } );
            submodel.getSemanticId().setSubmodel( submodel );
         }
      } );

      submodels.forEach( submodel -> {
         if ( submodel.getSubmodelSupplemSemanticIds() != null ) {
            submodel.getSubmodelSupplemSemanticIds().stream().filter( Objects::nonNull ).forEach( supplemental -> {
               supplemental.getKeys().forEach( key -> key.setSubmodelSupplemSemanticIdReference( supplemental ) );
               supplemental.setSubmodel( submodel );
            } );
         }
      } );
   }

   @Transactional
   public Shell findShellByExternalIdAndExternalSubjectId( String externalShellId, String externalSubjectId ) {
      final Optional<Shell> optionalShell;
      if ( shellAccessHandler.supportsGranularAccessControl() ) {
         optionalShell = shellRepository.findByIdExternal( externalShellId );
      } else {
         optionalShell = shellRepository.findByIdExternalAndExternalSubjectId( externalShellId, externalSubjectId,
               owningTenantId, externalSubjectIdWildcardPrefix, externalSubjectIdWildcardAllowedTypes );
      }
      return optionalShell
            .map( shell -> shellAccessHandler.filterShellProperties( shell, externalSubjectId ) )
            .orElseThrow( () -> new EntityNotFoundException( String.format( "Shell for identifier %s not found", externalShellId ) ) );
   }

   @Transactional
   public Shell findShellByExternalIdWithoutFiltering( String externalShellId ) {
      return doFindShellByExternalIdWithoutFiltering( externalShellId );
   }

   @Transactional( readOnly = true )
   public ShellCollectionDto findAllShells( Integer pageSize, String cursorVal, String externalSubjectId ) {

      pageSize = getPageSize( pageSize );
      ShellCursor cursor = new ShellCursor( pageSize, cursorVal );
      var specification = shellAccessHandler.shellFilterSpecification( SORT_FIELD_NAME_SHELL, cursor, externalSubjectId );
      final var foundList = new ArrayList<Shell>();
      boolean hasNext = true;
      //fetch 1 more item to make sure there is a visible item for the next page
      int fetchSize = pageSize + 1;
      while ( foundList.size() < fetchSize && hasNext ) {
         Page<Shell> currentPage = shellRepository.findAll( specification, ofSize( fetchSize ) );
         List<Shell> shells = shellAccessHandler.filterListOfShellProperties( currentPage.stream().toList(), externalSubjectId );
         shells.stream()
               .limit( (long) fetchSize - foundList.size() )
               .forEach( foundList::add );
         hasNext = currentPage.hasNext();
      }
      String nextCursor = null;

      final boolean hasNextPage = foundList.size() > pageSize;
      List<Shell> resultList = foundList.stream().limit( pageSize ).toList();
      if ( !resultList.isEmpty() ) {
         nextCursor = cursor.getEncodedCursorShell( resultList.get( resultList.size() - 1 ).getCreatedDate(), hasNextPage );
      }

      return ShellCollectionDto.builder()
            .items( resultList )
            .cursor( nextCursor )
            .build();
   }

   @Transactional( readOnly = true )
   public SubmodelCollectionDto findAllSubmodel( Integer pageSize, String cursorVal, Shell assetID ) {
      pageSize = getPageSize( pageSize );

      ShellCursor cursor = new ShellCursor( pageSize, cursorVal );
      var specification = new ShellSpecification<Submodel>( SORT_FIELD_NAME_SUBMODEL, cursor, null, null, null, null );
      Page<Submodel> shellPage = submodelRepository.findAll( Specification.allOf( hasShellFkId( assetID ).and( specification ) ),
            ofSize( cursor.getRecordSize() ) );

      var shellsPage = shellPage.getContent();
      String nextCursor = null;

      if ( !shellsPage.isEmpty() ) {
         nextCursor = cursor.getEncodedCursorSubmodel(
               shellsPage.get( shellsPage.size() - 1 ).getId(),
               shellPage.hasNext()
         );
      }
      return SubmodelCollectionDto.builder()
            .items( shellsPage )
            .cursor( nextCursor )
            .build();
   }

   private Integer getPageSize( Integer pageSize ) {
      return pageSize == null ? MAXIMUM_RECORDS : pageSize;
   }

   private Specification<Submodel> hasShellFkId( Shell shellId ) {
      return ( root, cq, cb ) -> cb.equal( root.get( "shellId" ), shellId );
   }

   @Transactional( readOnly = true )
   public GetAllAssetAdministrationShellIdsByAssetLink200Response findExternalShellIdsByIdentifiersByExactMatch( Set<ShellIdentifier> shellIdentifiers,
         Integer pageSize, String cursor, String externalSubjectId ) {

      pageSize = getPageSize( pageSize );
      try {
         List<String> keyValueCombinations = shellIdentifiers.stream().map( shellIdentifier -> shellIdentifier.getKey() + shellIdentifier.getValue() ).toList();

         //TODO: if we can define a sorting order, we should consider doing it on the database side
         final List<String> assetIdList = new ArrayList<>();
         String nextCursor;
         if ( shellAccessHandler.supportsGranularAccessControl() ) {
            List<ShellIdentifierMinimal> queryResults = shellIdentifierRepository
                  .findMinimalShellIdsBySpecificAssetIds( keyValueCombinations, keyValueCombinations.size() );
            Set<SpecificAssetId> userQuery = shellIdentifiers.stream()
                  .map( id -> new SpecificAssetId( id.getKey(), id.getValue() ) )
                  .collect( Collectors.toSet() );
            List<String> allVisible = shellAccessHandler.filterToVisibleShellIdsForLookup( userQuery, queryResults, externalSubjectId );
            allVisible.stream()
                  .skip( getCursorDecoded( cursor, allVisible ) )
                  .limit( pageSize )
                  .forEach( assetIdList::add );
            nextCursor = getCursorEncoded( allVisible, assetIdList );
         } else {
            List<String> queryResult = shellIdentifierRepository.findExternalShellIdsByIdentifiersByExactMatch( keyValueCombinations,
                  keyValueCombinations.size(), externalSubjectId, externalSubjectIdWildcardPrefix, externalSubjectIdWildcardAllowedTypes, owningTenantId,
                  ShellIdentifier.GLOBAL_ASSET_ID_KEY );
            pageSize = getPageSize( pageSize );

            int startIndex = getCursorDecoded( cursor, queryResult );
            queryResult.stream().skip( startIndex ).limit( pageSize ).forEach( assetIdList::add );

            nextCursor = getCursorEncoded( queryResult, assetIdList );
         }
         final var response = new GetAllAssetAdministrationShellIdsByAssetLink200Response();
         response.setResult( assetIdList );
         response.setPagingMetadata( new PagedResultPagingMetadata().cursor( nextCursor ) );
         return response;
      } catch ( DenyAccessException e ) {
         final var response = new GetAllAssetAdministrationShellIdsByAssetLink200Response();
         response.setResult( Collections.emptyList() );
         return response;
      }
   }

   private String getCursorEncoded( List<String> queryResult, List<String> assetIdList ) {
      if ( !queryResult.isEmpty() ) {
         if ( !assetIdList.get( assetIdList.size() - 1 ).equals( queryResult.get( queryResult.size() - 1 ) ) ) {
            String lastEle = assetIdList.get( assetIdList.size() - 1 );
            return Base64.getEncoder().encodeToString( lastEle.getBytes() );
         }
      }
      return null;
   }

   private String getCursorDecoded( String cursor ) {
      return Optional.ofNullable( cursor )
            .map( Base64.getDecoder()::decode )
            .map( String::new )
            .orElse( null );
   }

   private int getCursorDecoded( String cursor, List<String> queryResult ) {
      return Optional.ofNullable( getCursorDecoded( cursor ) )
            .map( decodedValue -> queryResult.indexOf( decodedValue ) + 1 )
            .orElse( 0 );
   }

   @Transactional( readOnly = true )
   public List<String> findExternalShellIdsByIdentifiersByAnyMatch( Set<ShellIdentifier> shellIdentifiers, String externalSubjectId ) {
      List<String> keyValueCombinations = shellIdentifiers.stream().map( shellIdentifier -> shellIdentifier.getKey() + shellIdentifier.getValue() ).toList();

      return shellRepository.findExternalShellIdsByIdentifiersByAnyMatch(
            keyValueCombinations,
            externalSubjectId,
            externalSubjectIdWildcardPrefix,
            externalSubjectIdWildcardAllowedTypes,
            owningTenantId,
            ShellIdentifier.GLOBAL_ASSET_ID_KEY );
   }

   // Not used in AAS3
   @Transactional( readOnly = true )
   public List<Shell> findShellsByExternalShellIds( Set<String> externalShellIds, String externalSubjectId ) {
      return shellRepository.findShellsByIdExternalIsIn( externalShellIds ).stream()
            .map( shell -> shellAccessHandler.filterShellProperties( shell, externalSubjectId ) )
            .collect( Collectors.toList() );
   }

   @Transactional
   public void update( Shell shell, String aasIdentifier ) {
      deleteShell( aasIdentifier );
      mapShellCollection( shell );
      mapSubmodel( shell.getSubmodels() );
      try {
         save( shell );
      } catch ( Exception e ) {
         throw new IllegalArgumentException( e.getMessage() );
      }
   }

   @Transactional
   public void deleteShell( String externalShellId ) {
      ShellMinimal shellFromDb = findShellMinimalByExternalId( externalShellId );
      shellRepository.deleteById( shellFromDb.getId() );
   }

   @Transactional( readOnly = true )
   public Set<ShellIdentifier> findShellIdentifiersByExternalShellId( String externalShellId, String externalSubjectId ) {
      return findShellByExternalIdAndExternalSubjectId( externalShellId, externalSubjectId ).getIdentifiers();
   }

   @Transactional
   public void deleteAllIdentifiers( String externalShellId ) {
      ShellMinimal shellFromDb = findShellMinimalByExternalId( externalShellId );
      shellIdentifierRepository.deleteShellIdentifiersByShellId( shellFromDb.getId(), ShellIdentifier.GLOBAL_ASSET_ID_KEY );
   }

   @Transactional
   public Set<ShellIdentifier> save( String externalShellId, Set<ShellIdentifier> shellIdentifiers, String externalSubjectId ) {
      Shell shellFromDb = doFindShellByExternalIdWithoutFiltering( externalShellId );

      List<ShellIdentifier> identifiersToUpdate = shellIdentifiers.stream().map( identifier -> identifier.withShellId( shellFromDb ) )
            .collect( Collectors.toList() );

      mapShellIdentifier( identifiersToUpdate.stream() );

      return ImmutableSet.copyOf( shellIdentifierRepository.saveAll( identifiersToUpdate ) );
   }

   private static void mapShellIdentifier( Stream<ShellIdentifier> identifiersToUpdate ) {
      identifiersToUpdate.filter( identifiers -> !identifiers.getKey().equalsIgnoreCase( "globalAssetId" ) ).forEach(
            identifier -> {
               if ( identifier.getSemanticId() != null ) {
                  identifier.getSemanticId().getKeys().forEach( key -> key.setShellIdentifierSemanticReference( identifier.getSemanticId() ) );
                  identifier.getSemanticId().setShellIdentifier( identifier );
               }

               if ( identifier.getSupplementalSemanticIds() != null ) {
                  identifier.getSupplementalSemanticIds().stream().filter( Objects::nonNull ).forEach( supplementalID -> {
                     supplementalID.getKeys().forEach( key -> key.setShellIdentifierSupplemSemanticReference( supplementalID ) );
                     supplementalID.setShellIdentifier( identifier );
                  } );
               }

               if ( identifier.getExternalSubjectId() != null ) {
                  identifier.getExternalSubjectId().getKeys().stream().filter( Objects::nonNull ).forEach( key -> {
                     key.setShellIdentifierExternalSubjectReference( identifier.getExternalSubjectId() );
                  } );
                  identifier.getExternalSubjectId().setShellIdentifier( identifier );
               }
            } );
   }

   @Transactional
   public Submodel save( String externalShellId, Submodel submodel, String externalSubjectId ) {
      Shell shellFromDb = doFindShellByExternalIdWithoutFiltering( externalShellId );
      submodel.setShellId( shellFromDb );

      //uniqueness on shellId and idShort
      boolean isIdShortPresent = Optional.of( shellFromDb ).map( Shell::getSubmodels ).stream().flatMap( Collection::stream )
            .map( Submodel::getIdShort )
            .anyMatch(
                  idShort -> idShort.equalsIgnoreCase( submodel.getIdShort() ) ); // check whether the input sub-model.idShort exists in DB

      if ( isIdShortPresent ) {// Throw exception if sub-model.idShort exists in DB
         throw new DuplicateKeyException( DUPLICATE_SUBMODEL_ID_SHORT_EXCEPTION );
      }
      return saveSubmodel( submodel );
   }

   public Submodel saveSubmodel( Submodel submodel ) {
      if ( submodelRepository.findByShellIdAndIdExternal( submodel.getShellId(), submodel.getIdExternal() ).isPresent() ) {
         throw new DuplicateKeyException( DUPLICATE_SUBMODEL_EXCEPTION );
      }
      return submodelRepository.save( submodel );
   }

   @Transactional
   public void update( String externalShellId, Submodel submodel, String externalSubjectId ) {
      Shell shellFromDb = doFindShellByExternalIdWithoutFiltering( externalShellId );
      shellFromDb.add( submodel );
      submodel.setShellId( shellFromDb );
      mapSubmodel( shellFromDb.getSubmodels() );
      submodelRepository.save( submodel );
   }

   @Transactional
   public void deleteSubmodel( String externalShellId, String externalSubModelId, String externalSubjectId ) {
      Shell shellFromDb = doFindShellByExternalIdWithoutFiltering( externalShellId );
      Submodel submodelId = findSubmodelMinimalByExternalId( shellFromDb.getId(), externalSubModelId );
      shellFromDb.getSubmodels().remove( submodelId );
      submodelRepository.deleteById( submodelId.getId() );
   }

   @Transactional( readOnly = true )
   public Submodel findSubmodelByExternalId( String externalShellId, String externalSubModelId, String externalSubjectId ) {
      Shell shellIdByExternalId = findShellByExternalIdAndExternalSubjectId( externalShellId, externalSubjectId );
      return submodelRepository
            .findByShellIdAndIdExternal( shellIdByExternalId, externalSubModelId )
            .orElseThrow( () -> new EntityNotFoundException( String.format( "Submodel for identifier %s not found.", externalSubModelId ) ) );
   }

   private Submodel findSubmodelMinimalByExternalId( UUID shellId, String externalSubModelId ) {
      return submodelRepository
            .findMinimalRepresentationByShellIdAndIdExternal( shellId, externalSubModelId )
            .orElseThrow( () -> new EntityNotFoundException( String.format( "Submodel for identifier %s not found.", externalSubModelId ) ) );
   }

   private ShellMinimal findShellMinimalByExternalId( String externalShellId ) {
      return shellRepository.findMinimalRepresentationByIdExternal( externalShellId )
            .orElseThrow( () -> new EntityNotFoundException( String.format( "Shell for identifier %s not found", externalShellId ) ) );
   }

   /**
    * Saves the provided shells. The transaction is scoped per shell. If saving of one shell fails others may succeed.
    *
    * @param shells the shells to save
    * @return the result of each save operation
    */
   public List<BatchResultDto> saveBatch( List<Shell> shells ) {
      return shells.stream().map( shell -> {
         try {
            shellRepository.save( shell );
            return new BatchResultDto( "AssetAdministrationShell successfully created.",
                  shell.getIdExternal(), HttpStatus.OK.value() );
         } catch ( Exception e ) {
            if ( e.getCause() instanceof DuplicateKeyException duplicateKeyException ) {
               return new BatchResultDto( duplicateKeyException.getMessage(),
                     shell.getIdExternal(),
                     HttpStatus.BAD_REQUEST.value() );
            }
            return new BatchResultDto( String.format( "Failed to create AssetAdministrationShell %s",
                  e.getMessage() ), shell.getIdExternal(), HttpStatus.BAD_REQUEST.value() );
         }
      } ).collect( Collectors.toList() );
   }

   public boolean hasAccessToShellWithVisibleSubmodelEndpoint( String endpointAddress, String externalSubjectId ) {
      List<Shell> shells = shellRepository.findAllBySubmodelEndpointAddress( endpointAddress );
      List<Shell> filtered = shellAccessHandler.filterListOfShellProperties( shells, externalSubjectId );
      return filtered.stream()
            .filter( Objects::nonNull )
            .anyMatch( shell -> shell.getSubmodels().stream()
                  .anyMatch( submodel -> submodel.getEndpoints().stream()
                        .anyMatch( endpoint -> Objects.equals( endpointAddress, endpoint.getEndpointAddress() ) ) ) );
   }

   private Shell doFindShellByExternalIdWithoutFiltering( String externalShellId ) {
      return shellRepository.findByIdExternal( externalShellId )
            .orElseThrow( () -> new EntityNotFoundException( String.format( "Shell for identifier %s not found", externalShellId ) ) );
   }
}
