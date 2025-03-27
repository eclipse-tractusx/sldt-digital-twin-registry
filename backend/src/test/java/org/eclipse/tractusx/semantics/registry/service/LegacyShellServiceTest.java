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
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.InlineResponse200;
import org.eclipse.tractusx.semantics.aas.registry.model.PagedResultPagingMetadata;
import org.eclipse.tractusx.semantics.registry.TestUtil;
import org.eclipse.tractusx.semantics.registry.mapper.ShellMapper;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties( RegistryProperties.class )
class LegacyShellServiceTest {

   protected static final String TENANT_TWO = "TENANT_TWO";

   @Autowired
   private ShellService shellService;
   @Autowired
   private ShellMapper shellMapper;
   protected String keyPrefix;

   @BeforeEach
   void setUp() {
      keyPrefix = UUID.randomUUID().toString();
   }

   @Test
   void testsLookupWithNoMatchingRecordsExpectEmptyListAndNoCursor() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, null, null, TENANT_TWO, null );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().isEmpty();
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNull();
   }

   @Test
   void testsLookupWithLessThanAPageOfMatchingRecordsExpectPartialListAndNoCursorAndValidCreatedDate() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final String id = UUID.randomUUID().toString();
      createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, 5, null, TENANT_TWO, OffsetDateTime.parse( "2025-03-24T00:00:00.000000Z" ) );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( 1 ).contains( id );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNull();
   }

   @Test
   void testsLookupWithThreePagesOfMatchingRecordsRequestingSecondPageExpectFullListAndCursorAndValidCreatedDate() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final int pageSize = 5;
      final int totalItems = pageSize * 3;
      final List<String> expectedIds = IntStream.range( 0, totalItems )
            .mapToObj( i -> UUID.randomUUID().toString() )
            .toList();
      expectedIds.forEach( id -> createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue ) );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, pageSize, toCursor( expectedIds, pageSize - 1 ), TENANT_TWO, OffsetDateTime.parse( "2025-03-24T00:00:00.000000Z" ) );

      final var actualWithPageLimitOne = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, 1, null, TENANT_TWO, OffsetDateTime.parse( "2025-03-24T00:00:00.000000Z" ) );

      assertThat( actualWithPageLimitOne ).isNotNull().extracting( InlineResponse200::getPagingMetadata ).extracting( PagedResultPagingMetadata::getCursor )
            .isNotNull();
      assertThat( actualWithPageLimitOne ).isNotNull().extracting( InlineResponse200::getResult ).isNotNull().extracting( List::size ).isEqualTo( 1 );

      final var actualWithCursor = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, 1, actualWithPageLimitOne.getPagingMetadata().getCursor(), TENANT_TWO, OffsetDateTime.parse( "2025-03-24T00:00:00.000000Z" ) );

      assertThat( actualWithCursor ).isNotNull().extracting( InlineResponse200::getPagingMetadata ).extracting( PagedResultPagingMetadata::getCursor )
            .isNotNull();
      assertThat( actualWithCursor ).isNotNull().extracting( InlineResponse200::getResult ).isNotNull().extracting( List::size ).isEqualTo( 1 );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( pageSize ).containsAll( expectedIds.subList( pageSize, pageSize * 2 ) );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNotNull()
            .isEqualTo( toCursor( expectedIds, pageSize * 2 - 1 ) );

   }

   @Test
   void testsLookupWithLessThanAPageOfMatchingRecordsExpectPartialListAndNoCursorAndInValidCreatedDate() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final String id = UUID.randomUUID().toString();
      createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, 5, null, TENANT_TWO, OffsetDateTime.parse( "2225-03-24T00:00:00.000000Z" ) );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().isEmpty();
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNull();
   }

   @Test
   void testsLookupWithLessThanAPageOfMatchingRecordsExpectPartialListAndNoCursor() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final String id = UUID.randomUUID().toString();
      createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, 5, null, TENANT_TWO );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( 1 ).contains( id );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNull();
   }

   @Test
   void testsLookupWithExactlyOnePageOfMatchingRecordsExpectFullListAndNoCursor() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final List<String> expectedIds = IntStream.rangeClosed( 0, 4 )
            .mapToObj( i -> UUID.randomUUID().toString() )
            .toList();
      expectedIds.forEach( id -> createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue ) );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, 5, null, TENANT_TWO );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( expectedIds.size() ).containsAll( expectedIds );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNull();
   }

   @Test
   void testsLookupWithOneMoreThanOnePageOfMatchingRecordsExpectFullListAndCursor() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final int pageSize = 5;
      final int totalItems = pageSize + 1;
      final List<String> expectedIds = IntStream.range( 0, totalItems )
            .mapToObj( i -> UUID.randomUUID().toString() )
            .toList();
      expectedIds.forEach( id -> createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue ) );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, pageSize, null, TENANT_TWO );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( pageSize ).containsAll( expectedIds.subList( 0, pageSize ) );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNotNull()
            .isEqualTo( toCursor( expectedIds, pageSize - 1 ) );
   }

   @Test
   void testsLookupWithTwoPagesOfMatchingRecordsExpectFullListAndCursor() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final int pageSize = 5;
      final int totalItems = pageSize + pageSize;
      final List<String> expectedIds = IntStream.range( 0, totalItems )
            .mapToObj( i -> UUID.randomUUID().toString() )
            .toList();
      expectedIds.forEach( id -> createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue ) );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, pageSize, null, TENANT_TWO );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( pageSize ).containsAll( expectedIds.subList( 0, pageSize ) );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNotNull()
            .isEqualTo( toCursor( expectedIds, pageSize - 1 ) );
   }

   @Test
   void testsLookupWithThreePagesOfMatchingRecordsRequestingSecondPageExpectFullListAndCursor() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final int pageSize = 5;
      final int totalItems = pageSize * 3;
      final List<String> expectedIds = IntStream.range( 0, totalItems )
            .mapToObj( i -> UUID.randomUUID().toString() )
            .toList();
      expectedIds.forEach( id -> createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue ) );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, pageSize, toCursor( expectedIds, pageSize - 1 ), TENANT_TWO, null );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( pageSize ).containsAll( expectedIds.subList( pageSize, pageSize * 2 ) );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNotNull()
            .isEqualTo( toCursor( expectedIds, pageSize * 2 - 1 ) );
   }

   @Test
   void testsLookupWithThreePagesOfMatchingRecordsRequestingPageOfOnlyLastItemExpectSingleItemAndNoCursor() {
      final String specificAssetIdName = keyPrefix + "key";
      final String specificAssetIdValue = "value";
      final Set<ShellIdentifier> criteria = Set.of( new ShellIdentifier().withKey( specificAssetIdName ).withValue( specificAssetIdValue ) );
      final int pageSize = 5;
      final int totalItems = pageSize * 3;
      final List<String> expectedIds = IntStream.range( 0, totalItems )
            .mapToObj( i -> UUID.randomUUID().toString() )
            .toList();
      expectedIds.forEach( id -> createShellWithIdAndSpecificAssetIds( id, specificAssetIdName, specificAssetIdValue ) );

      final var actual = shellService.findExternalShellIdsByIdentifiersByExactMatch(
            criteria, pageSize, toCursor( expectedIds, pageSize * 3 - 2 ), TENANT_TWO );

      assertThat( actual ).isNotNull();
      assertThat( actual.getResult() ).isNotNull().hasSize( 1 ).containsAll( expectedIds.subList( pageSize * 3 - 1, pageSize * 3 ) );
      assertThat( actual.getPagingMetadata() ).isNotNull();
      assertThat( actual.getPagingMetadata().getCursor() ).isNull();
   }

   private String toCursor( final List<String> expectedIds, final int indexOfLastVisibleId ) {
      return new String( Base64.getUrlEncoder().encode( expectedIds.get( indexOfLastVisibleId ).getBytes() ) );
   }

   private void createShellWithIdAndSpecificAssetIds( final String id, final String specificAssetIdName, final String specificAssetIdValue ) {
      final AssetAdministrationShellDescriptor shellDescriptor = TestUtil.createCompleteAasDescriptor();
      shellDescriptor.setId( id );
      shellDescriptor.setSpecificAssetIds( List.of( TestUtil.createSpecificAssetId( specificAssetIdName, specificAssetIdValue, List.of( TENANT_TWO ) ) ) );
      final Shell shell = shellMapper.fromApiDto( shellDescriptor );
      shellService.mapShellCollection( shell );
      if ( !shell.getSubmodels().isEmpty() ) {
         shellService.mapSubmodel( shell.getSubmodels() );
      }
      shellService.save( shell );
   }
}