/*******************************************************************************
 * Copyright (c) 2025 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.aas.registry.model.InlineResponse200;
import org.eclipse.tractusx.semantics.aas.registry.model.PagedResultPagingMetadata;
import org.eclipse.tractusx.semantics.accesscontrol.api.AccessControlRuleService;
import org.eclipse.tractusx.semantics.aas.registry.model.SearchAllAssetAdministrationShellIdsByAssetLink200Response;
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
import org.hibernate.sql.results.internal.TupleImpl;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSet;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShellService {

   public static final String DUPLICATE_SUBMODEL_EXCEPTION = "An AssetAdministrationSubmodel for the given identification does already exists.";
   private static final String SORT_FIELD_NAME_SHELL = "createdDate";
   private static final String SORT_FIELD_NAME_SUBMODEL = "id";
   private static final String DEFAULT_EXTERNAL_ID = "00000000-0000-0000-0000-000000000000";
   private static final Instant MINIMUM_SQL_DATETIME = OffsetDateTime
         .of( 1800, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ).toInstant();
   private static final int MAXIMUM_RECORDS = 1000;
   private static final int DEFAULT_FETCH_SIZE = 500;

   private final ShellRepository shellRepository;
   private final ShellIdentifierRepository shellIdentifierRepository;
   private final SubmodelRepository submodelRepository;
   private final ShellAccessHandler shellAccessHandler;
   private final String owningTenantId;
   private final String externalSubjectIdWildcardPrefix;
   private final List<String> externalSubjectIdWildcardAllowedTypes;
   private final int granularAccessControlFetchSize;
   private final boolean isGranularAccessControlEnabled;
   private final AccessControlRuleService accessControlRuleService;

   public ShellService( ShellRepository shellRepository,
         ShellIdentifierRepository shellIdentifierRepository,
         SubmodelRepository submodelRepository,
         RegistryProperties registryProperties,
         ShellAccessHandler shellAccessHandler,
         AccessControlRuleService accessControlRuleService) {
      this.shellRepository = shellRepository;
      this.shellIdentifierRepository = shellIdentifierRepository;
      this.submodelRepository = submodelRepository;
      this.shellAccessHandler = shellAccessHandler;
      this.owningTenantId = registryProperties.getIdm().getOwningTenantId();
      this.externalSubjectIdWildcardPrefix = registryProperties.getExternalSubjectIdWildcardPrefix();
      this.externalSubjectIdWildcardAllowedTypes = registryProperties.getExternalSubjectIdWildcardAllowedTypes();
      this.granularAccessControlFetchSize = Optional.ofNullable( registryProperties.getGranularAccessControlFetchSize() ).orElse( DEFAULT_FETCH_SIZE );
      this.isGranularAccessControlEnabled = registryProperties.getUseGranularAccessControl();
      this.accessControlRuleService = accessControlRuleService;
   }

   @Transactional
   public Shell save( Shell shell ) {
      if ( shellRepository.findByIdExternal( shell.getIdExternal() ).isPresent() ) {
         throw new DuplicateKeyException( "An AssetAdministrationShell for the given identification does already exists." );
      }

      return shellRepository.save( shell );
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

   /**
    * Creates a JPA Specification for fetching all associations of the Shell entity.
    * This method is used to define a query that fetches all related entities of a Shell,
    * including identifiers, descriptions, display names, submodels, and their nested associations.
    *
    * The specification ensures that:
    * - Fetching is only applied to entity queries (not count queries).
    * - The query result is distinct to avoid duplicate records.
    * - Nested associations are fetched using LEFT JOINs to include all related data.
    *
    * @return Specification<Shell> A JPA Specification for fetching all associations of the Shell entity.
    */
   public static Specification<Shell> withAllAssociations() {
       return (root, query, criteriaBuilder) -> {
           // Only apply fetching for entity queries, not for count queries
           if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
               // Set distinct to true to avoid duplicates
               query.distinct(true);

               // Root level fetches
               Fetch<Shell, ?> identifiersFetch = root.fetch("identifiers", JoinType.LEFT);
               root.fetch("descriptions", JoinType.LEFT);
               root.fetch("displayNames", JoinType.LEFT);
               Fetch<Shell, ?> submodelsFetch = root.fetch("submodels", JoinType.LEFT);

               // Second level fetches for identifiers
               identifiersFetch.fetch("externalSubjectId", JoinType.LEFT);
               identifiersFetch.fetch("semanticId", JoinType.LEFT);
               identifiersFetch.fetch("supplementalSemanticIds", JoinType.LEFT);

               // Second level fetches for submodels
               Fetch<?, ?> semanticIdFetch = submodelsFetch.fetch("semanticId", JoinType.LEFT);
               submodelsFetch.fetch("submodelSupplemSemanticIds", JoinType.LEFT);
               submodelsFetch.fetch("displayNames", JoinType.LEFT);
               submodelsFetch.fetch("descriptions", JoinType.LEFT);
               Fetch<?, ?> endpointsFetch = submodelsFetch.fetch("endpoints", JoinType.LEFT);

               // Third level fetches
               semanticIdFetch.fetch("keys", JoinType.LEFT);
               endpointsFetch.fetch("submodelSecurityAttribute", JoinType.LEFT);
           }
           return criteriaBuilder.conjunction();
       };
   }

   @Transactional( readOnly = true )
   public ShellCollectionDto findAllShells(Integer pageSize, final String cursorVal, final String externalSubjectId, final OffsetDateTime createdAfter) {

      pageSize = getPageSize( pageSize );
      ShellCursor cursor = new ShellCursor(pageSize, cursorVal);

     // Instant cursorCreatedDate = cursor.getShellSearchCursor();
      String cVal = cursorVal;
      if(cVal == null){cVal = DEFAULT_EXTERNAL_ID;}
      Instant cursorCreatedDate = getCreatedDate(cVal,cursorVal!=null,createdAfter);

      String extSubId = null;
      if(!externalSubjectId.isEmpty()){
         extSubId = externalSubjectId;
      }

      Page<Shell> shellPage = shellRepository.findAllByExternalSubjectId(
              extSubId,
              owningTenantId,
              externalSubjectIdWildcardPrefix,
              externalSubjectIdWildcardAllowedTypes,
              cursorCreatedDate,
              PageRequest.of(0, pageSize, Sort.by("created_date").ascending())
      );

      //Page to List
      List<Shell> shells = shellAccessHandler.filterListOfShellProperties( shellPage.stream().toList(), externalSubjectId );

      String nextCursor = null;
      if (!shells.isEmpty()) {
         Instant lastDate = shells.get(shells.size() - 1).getCreatedDate();
         nextCursor = cursor.getEncodedCursorShell(lastDate, shells.size() == pageSize);
      }

      return ShellCollectionDto.builder()
              .items( shells )
              .cursor( nextCursor )
              .build();
   }

   /**
    * Adds mandatory specific asset ID filters to the given JPA Specification.
    *
    * This method modifies the provided Specification to include filtering conditions
    * based on specific asset IDs and their values. It ensures that only entities
    * matching the specified criteria are included in the query results.
    *
    * The filtering is applied only if granular access control is enabled and the
    * `externalSubjectId` does not match the owning tenant ID.
    *
    * @param externalSubjectId The external subject ID of the user making the request.
    * @param specification The existing JPA Specification to be modified.
    * @return Specification<Shell> The modified Specification with additional filtering conditions.
    */
   private Specification<Shell> setMandatorySpecificIdsAndValues(String externalSubjectId, Specification<Shell> specification) {
    if(isGranularAccessControlEnabled && !owningTenantId.equals( externalSubjectId )) {
       var specificAssetIdNameAndNameSet =  accessControlRuleService.findAllByBpnWithinValidityPeriod(externalSubjectId,Instant.now());

            // Add filtering by specific asset IDs to the specification
           specification = specification.and((root, query, criteriaBuilder) -> {
               // Skip if there are no identifiers to filter by
               if (specificAssetIdNameAndNameSet == null || specificAssetIdNameAndNameSet.isEmpty()) {
                   return criteriaBuilder.conjunction(); // always true
               }

               // Ensure distinct results to avoid duplicates
               Optional.ofNullable( query ).ifPresent( queryDetails -> queryDetails.distinct(true));

               // Join Shell with ShellIdentifier entities
               var identifierJoin = root.join("identifiers", JoinType.LEFT);

               // Create predicates for each key-value pair
               var predicates =  specificAssetIdNameAndNameSet.entrySet().stream().filter( stringSetEntry -> CollectionUtils.isNotEmpty( stringSetEntry.getValue() ))
               .map( stringSetEntry -> {
                  Predicate keyPredicate = criteriaBuilder.equal(identifierJoin.get("key"), stringSetEntry.getKey());
                  Predicate valuePredicate = identifierJoin.get("value").in(stringSetEntry.getValue());
                  return criteriaBuilder.and(keyPredicate, valuePredicate);
               }).toList();

               // If no valid predicates were created, return always true
               if (CollectionUtils.isEmpty( predicates )) {
                   return criteriaBuilder.conjunction();
               }

               // Return OR of all key-value pair predicates (shell matches if it has ANY of the pairs)
               return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
           });

      }
    return specification;
   }

   @Transactional( readOnly = true )
   public SubmodelCollectionDto findAllSubmodel( Integer pageSize, String cursorVal, Shell assetID ) {
      pageSize = getPageSize( pageSize );

      ShellCursor cursor = new ShellCursor( pageSize, cursorVal );
      var specification = new ShellSpecification<Submodel>( SORT_FIELD_NAME_SUBMODEL, cursor, null, null, null, null,null );
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

   private Specification<Submodel> hasShellFkId( final Shell shellId ) {
      return ( root, cq, cb ) -> cb.equal( root.get( "shellId" ), shellId );
   }

   @Transactional( readOnly = true )
   public InlineResponse200 findExternalShellIdsByIdentifiersByExactMatch( final Set<ShellIdentifier> shellIdentifiers, Integer pageSize, final String cursor,
         final String externalSubjectId, final OffsetDateTime createdAfter ) {

      pageSize = getPageSize( pageSize );
      final boolean isCursorAvailable = StringUtils.isNotBlank( cursor );
      final String cursorValue = getCursorDecoded( cursor ).orElse( DEFAULT_EXTERNAL_ID );

      final List<String> assetIdList;
      final String nextCursor;

      try {
         final Pair<List<String>,Long> visibleAssetIds;
         if ( shellAccessHandler.supportsGranularAccessControl() ) {
            visibleAssetIds = fetchAPageOfAasIdsUsingGranularAccessControl( shellIdentifiers, externalSubjectId, cursorValue, pageSize, isCursorAvailable,
                  createdAfter );
            assetIdList = visibleAssetIds.a;
            nextCursor = getCursorEncoded(assetIdList, visibleAssetIds.b);
         } else {
            visibleAssetIds = fetchAPageOfAasIdsUsingLegacyAccessControl( shellIdentifiers, externalSubjectId, cursorValue, pageSize, isCursorAvailable,
                  createdAfter );
            assetIdList = visibleAssetIds.a.stream().limit( pageSize ).toList();
            nextCursor = getCursorEncoded( visibleAssetIds.a, assetIdList );
         }

         final var response = new InlineResponse200();
         response.setResult( assetIdList );
         response.setPagingMetadata( new PagedResultPagingMetadata().cursor( nextCursor ) );
         return response;
      } catch ( DenyAccessException e ) {
         final var response = new InlineResponse200();
         response.setResult( Collections.emptyList() );
         return response;
      }
   }

   @Transactional( readOnly = true )
   public SearchAllAssetAdministrationShellIdsByAssetLink200Response findExternalShellIdsByAssetLinkByExactMatch( Set<ShellIdentifier> shellIdentifiers,
         Integer pageSize, String cursor, String externalSubjectId ) {

      pageSize = getPageSize( pageSize );
      final String cursorValue = getCursorDecoded( cursor ).orElse( DEFAULT_EXTERNAL_ID );


      final List<String> assetIdList;
      final String nextCursor;

      try {
         final Pair<List<String>,Long> visibleAssetIds;
         if ( shellAccessHandler.supportsGranularAccessControl() ) {
            visibleAssetIds = fetchAPageOfAasIdsUsingGranularAccessControl( shellIdentifiers, externalSubjectId, cursorValue, pageSize, false,
                    null );
            assetIdList = visibleAssetIds.a;
            nextCursor = getCursorEncoded(assetIdList, visibleAssetIds.b);
         } else {
            visibleAssetIds = fetchAPageOfAasIdsUsingLegacyAccessControl( shellIdentifiers, externalSubjectId, cursorValue, pageSize, false,
                    null );
            assetIdList = visibleAssetIds.a.stream().limit( pageSize ).toList();
            nextCursor = getCursorEncoded( visibleAssetIds.a, assetIdList );
         }

         final var response = new SearchAllAssetAdministrationShellIdsByAssetLink200Response();
         response.setResult( assetIdList );
         response.setPagingMetadata( new PagedResultPagingMetadata().cursor( nextCursor ) );
         return response;
      } catch ( DenyAccessException e ) {
         final var response = new SearchAllAssetAdministrationShellIdsByAssetLink200Response();
         response.setResult( Collections.emptyList() );
         return response;
      }
   }

   private Pair<List<String>,Long> fetchAPageOfAasIdsUsingLegacyAccessControl( final Set<ShellIdentifier> shellIdentifiers, final String externalSubjectId,
         final String cursorValue, final int pageSize, final boolean isCursorAvailable, final OffsetDateTime createdAfter ) {
      final var fetchSize = pageSize + 1;
      final Instant cutoffDate = getCreatedDate( cursorValue, isCursorAvailable, createdAfter );
      List<String> keyValueCombinations = toKeyValueCombinations( shellIdentifiers );

      List<String> foundShells = shellIdentifierRepository.findExternalShellIdsByIdentifiersByExactMatch( keyValueCombinations,
              keyValueCombinations.size(), externalSubjectId, externalSubjectIdWildcardPrefix, externalSubjectIdWildcardAllowedTypes, owningTenantId,
              ShellIdentifier.GLOBAL_ASSET_ID_KEY, cutoffDate, cursorValue, fetchSize);

      return new Pair<>(foundShells, (long) foundShells.size());
   }

   /**
    * Retrieves the created date based on the cursor value and the availability of the cursor.
    *
    * <p>This method determines the created date to be used in queries. If the cursor is available,
    * it fetches the created date from the repository using the cursor value. If the cursor is not
    * available and the cursor value matches the default external ID, it uses the provided
    * `createdAfter` date or fetches the created date from the repository. Otherwise, it defaults
    * to the minimum SQL datetime.</p>
    *
    * @param cursorValue the value of the cursor, used to identify the entity
    * @param isCursorAvailable a flag indicating whether the cursor is available
    * @param createdAfter the date after which entities were created, used as a fallback
    * @return the created date as an {@link Instant}
    */
   private Instant getCreatedDate( final String cursorValue, final boolean isCursorAvailable, final OffsetDateTime createdAfter ) {
      if ( isCursorAvailable ) {
         // Fetch the created date from the repository using the cursor value
         return shellRepository.getCreatedDateByIdExternal( cursorValue ).orElse( MINIMUM_SQL_DATETIME );
      }
      if ( cursorValue.equalsIgnoreCase( DEFAULT_EXTERNAL_ID ) ) {
         // Use the provided createdAfter date or fetch from the repository as a fallback
         return Optional.ofNullable( createdAfter )
               .map( OffsetDateTime::toInstant )
               .orElseGet( () -> shellRepository.getCreatedDateByIdExternal( cursorValue ).orElse( MINIMUM_SQL_DATETIME ) );
      }
      // Default case: fetch the created date from the repository or use the minimum SQL datetime
      return shellRepository.getCreatedDateByIdExternal( cursorValue ).orElse( MINIMUM_SQL_DATETIME );
   }

   private Pair<List<String>, Long>  fetchAPageOfAasIdsUsingGranularAccessControl(final Set<ShellIdentifier> shellIdentifiers, final String externalSubjectId,
                                                                           final String cursorValue, final int pageSize, final boolean isCursorAvailable, final OffsetDateTime createdAfter ) throws DenyAccessException {

      final Set<SpecificAssetId> userQuery = shellIdentifiers.stream()
            .map( id -> new SpecificAssetId( id.getKey(), id.getValue() ) )
            .collect( Collectors.toSet() );

      List<String> keyValueCombinations = toKeyValueCombinations( shellIdentifiers );

      final Instant currentCutoffDate = getCreatedDate( cursorValue, isCursorAvailable, createdAfter );

      String extSubId = null;
      if(!externalSubjectId.isEmpty()){
         extSubId = externalSubjectId;
      }

      Page<UUID> shellIds = shellIdentifierRepository.findAPageOfShellIdsBySpecificAssetIds(
               keyValueCombinations, keyValueCombinations.size(), currentCutoffDate, extSubId, owningTenantId, externalSubjectIdWildcardPrefix, externalSubjectIdWildcardAllowedTypes, PageRequest.ofSize( pageSize ) );

      List<ShellIdentifierMinimal> queryResults = shellIdentifierRepository
               .findMinimalShellIdsByShellIds(shellIds.stream().toList());

      List<String> visibleAssetIds = shellAccessHandler.filterToVisibleShellIdsForLookup( userQuery, queryResults, extSubId );

      return new Pair<List<String>, Long> (visibleAssetIds, shellIds.getTotalElements());
   }

   @Transactional( readOnly = true )
   public List<String> findExternalShellIdsByIdentifiersByAnyMatch( Set<ShellIdentifier> shellIdentifiers, String externalSubjectId ) {
      List<String> keyValueCombinations = toKeyValueCombinations( shellIdentifiers );

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

   private void mapShellIdentifier( Stream<ShellIdentifier> identifiersToUpdate ) {
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

   @Transactional( readOnly = true )
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

   private <T> T lastItemOf( List<T> list ) {
      return list.get( list.size() - 1 );
   }

   private List<String> toKeyValueCombinations( Set<ShellIdentifier> shellIdentifiers ) {
      return shellIdentifiers.stream()
            .map( shellIdentifier -> shellIdentifier.getKey() + shellIdentifier.getValue() )
            .toList();
   }

   private String getCursorEncoded( List<String> queryResult, List<String> assetIdList ) {
      if ( !queryResult.isEmpty() && !lastItemOf( assetIdList ).equals( lastItemOf( queryResult ) ) ) {
         return Base64.getEncoder().encodeToString( lastItemOf( assetIdList ).getBytes() );
      }
      return null;
   }

   private String getCursorEncoded( List<String> assetIdList , Long totalElementCount ) {
      if ( !assetIdList.isEmpty() && assetIdList.size() < totalElementCount) {
         return Base64.getEncoder().encodeToString( lastItemOf( assetIdList ).getBytes() );
      }
      return null;
   }

   private Optional<String> getCursorDecoded( String cursor ) {
      return Optional.ofNullable( cursor )
            .map( Base64.getDecoder()::decode )
            .map( String::new );
   }
}
