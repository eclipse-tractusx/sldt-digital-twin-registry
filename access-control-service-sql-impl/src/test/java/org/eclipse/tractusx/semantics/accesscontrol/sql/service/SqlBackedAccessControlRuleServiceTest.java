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

package org.eclipse.tractusx.semantics.accesscontrol.sql.service;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityContext;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.repository.AccessControlRuleRepository;
import org.eclipse.tractusx.semantics.accesscontrol.sql.repository.FileBasedAccessControlRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;

class SqlBackedAccessControlRuleServiceTest {

   private static final String MANUFACTURER_PART_ID = "manufacturerPartId";
   private static final String CUSTOMER_PART_ID = "customerPartId";
   private static final String PART_INSTANCE_ID = "partInstanceId";
   private static final String VERSION_NUMBER = "versionNumber";
   private static final String REVISION_NUMBER = "revisionNumber";
   private static final SpecificAssetId MANUFACTURER_PART_ID_99991 = new SpecificAssetId( MANUFACTURER_PART_ID, "99991" );
   private static final SpecificAssetId CUSTOMER_PART_ID_ACME001 = new SpecificAssetId( CUSTOMER_PART_ID, "ACME001" );
   private static final SpecificAssetId CUSTOMER_PART_ID_CONTOSO001 = new SpecificAssetId( CUSTOMER_PART_ID, "CONTOSO001" );
   private static final SpecificAssetId PART_INSTANCE_ID_00001 = new SpecificAssetId( PART_INSTANCE_ID, "00001" );
   private static final SpecificAssetId PART_INSTANCE_ID_00002 = new SpecificAssetId( PART_INSTANCE_ID, "00002" );
   private static final SpecificAssetId VERSION_NUMBER_01 = new SpecificAssetId( VERSION_NUMBER, "01" );
   private static final SpecificAssetId REVISION_NUMBER_01 = new SpecificAssetId( REVISION_NUMBER, "01" );
   private static final SpecificAssetId REVISION_NUMBER_02 = new SpecificAssetId( REVISION_NUMBER, "02" );
   private static final String BPNA = "BPNL00000000000A";
   private static final String BPNB = "BPNL00000000000B";
   private static final String BPNC = "BPNL00000000000C";
   private static final String TRACEABILITYV_1_1_0 = "Traceability" + "v1.1.0";
   private static final String PRODUCT_CARBON_FOOTPRINTV_1_1_0 = "ProductCarbonFootprintv1.1.0";
   private SqlBackedAccessControlRuleService underTest;

   public static Stream<Arguments> matchingSpecificAssetIdFilterProvider() {
      return Stream.<Arguments> builder()
            .add( Arguments.of(
                  Set.of( MANUFACTURER_PART_ID_99991, CUSTOMER_PART_ID_ACME001, PART_INSTANCE_ID_00001, REVISION_NUMBER_02 ),
                  BPNA,
                  false ) ) //the rules will hide some of the query parameters
            .add( Arguments.of(
                  Set.of( MANUFACTURER_PART_ID_99991, CUSTOMER_PART_ID_ACME001, PART_INSTANCE_ID_00002, VERSION_NUMBER_01, REVISION_NUMBER_01 ),
                  BPNA,
                  false ) ) //the rules will hide some of the query parameters
            .add( Arguments.of(
                  Set.of( MANUFACTURER_PART_ID_99991, CUSTOMER_PART_ID_ACME001, PART_INSTANCE_ID_00001, VERSION_NUMBER_01, REVISION_NUMBER_01 ),
                  BPNA,
                  true ) )
            .build();
   }

   public static Stream<Arguments> matchingSpecificAssetIdVisibilityProvider() {
      return Stream.<Arguments> builder()
            .add( Arguments.of(
                  Set.of( MANUFACTURER_PART_ID_99991, CUSTOMER_PART_ID_ACME001, PART_INSTANCE_ID_00001, VERSION_NUMBER_01 ),
                  BPNA,
                  Set.of( MANUFACTURER_PART_ID, CUSTOMER_PART_ID, PART_INSTANCE_ID, VERSION_NUMBER ),
                  Set.of( TRACEABILITYV_1_1_0 ) ) )
            .add( Arguments.of(
                  Set.of( MANUFACTURER_PART_ID_99991, CUSTOMER_PART_ID_ACME001, PART_INSTANCE_ID_00002, REVISION_NUMBER_01 ),
                  BPNA,
                  Set.of( MANUFACTURER_PART_ID, CUSTOMER_PART_ID, PART_INSTANCE_ID, REVISION_NUMBER ),
                  Set.of( PRODUCT_CARBON_FOOTPRINTV_1_1_0 ) ) )
            .add( Arguments.of(
                  Set.of( MANUFACTURER_PART_ID_99991, CUSTOMER_PART_ID_ACME001, PART_INSTANCE_ID_00001, VERSION_NUMBER_01, REVISION_NUMBER_01 ),
                  BPNA,
                  Set.of( MANUFACTURER_PART_ID, CUSTOMER_PART_ID, PART_INSTANCE_ID, REVISION_NUMBER, VERSION_NUMBER ),
                  Set.of( PRODUCT_CARBON_FOOTPRINTV_1_1_0, TRACEABILITYV_1_1_0 ) ) )
            .build();
   }

   @BeforeEach
   void setUp() {
      ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
      final var filePath = Path.of( getClass().getResource( "/example-access-rules.json" ).getFile() );
      AccessControlRuleRepository repository = new FileBasedAccessControlRuleRepository( objectMapper, filePath.toAbsolutePath().toString() );
      underTest = new SqlBackedAccessControlRuleService( repository, "PUBLIC_READABLE" );
   }

   @Test
   void testFilterValidSpecificAssetIdsForLookupWhenNoMatchingSpecificAssetIdsProvidedExpectException() {
      final var specificAssetIds = new HashSet<SpecificAssetId>();
      final var shellContexts = List.of( new ShellVisibilityContext( UUID.randomUUID().toString(), specificAssetIds ) );

      assertThatThrownBy( () -> underTest.filterValidSpecificAssetIdsForLookup( specificAssetIds, shellContexts, BPNB ) )
            .isInstanceOf( DenyAccessException.class );
   }

   @ParameterizedTest
   @MethodSource( "matchingSpecificAssetIdFilterProvider" )
   void testFilterValidSpecificAssetIdsForLookupWhenMatchingSpecificAssetIdsProvidedExpectFilteredIds(
         Set<SpecificAssetId> specificAssetIds, String bpn, boolean shouldMatch ) throws DenyAccessException {
      final var aasId = UUID.randomUUID().toString();
      final var shellContext = List.of( new ShellVisibilityContext( aasId, specificAssetIds ) );
      final var actual = underTest.filterValidSpecificAssetIdsForLookup( specificAssetIds, shellContext, bpn );

      if ( shouldMatch ) {
         assertThat( actual ).isEqualTo( List.of( aasId ) );
      } else {
         assertThat( actual ).isEmpty();
      }
   }

   @Test
   void testFetchVisibilityCriteriaForShellWhenNoMatchingBpnExpectException() {
      final var specificAssetIds = Set.of( MANUFACTURER_PART_ID_99991, CUSTOMER_PART_ID_CONTOSO001, REVISION_NUMBER_01 );
      ShellVisibilityContext shellContext = new ShellVisibilityContext( UUID.randomUUID().toString(), specificAssetIds );

      assertThatThrownBy( () -> underTest.fetchVisibilityCriteriaForShell( shellContext, BPNB ) )
            .isInstanceOf( DenyAccessException.class );
   }

   @ParameterizedTest
   @MethodSource( "matchingSpecificAssetIdVisibilityProvider" )
   void testFetchVisibilityCriteriaForShellWhenMatchingSpecificAssetIdsProvidedExpectFilteringList(
         Set<SpecificAssetId> specificAssetIds, String bpn,
         Set<String> expectedSpecificAssetIdNames, Set<String> expectedSemanticIds ) throws DenyAccessException {
      ShellVisibilityContext shellContext = new ShellVisibilityContext( UUID.randomUUID().toString(), specificAssetIds );

      final var actual = underTest.fetchVisibilityCriteriaForShell( shellContext, bpn );

      assertThat( actual.visibleSemanticIds() ).isEqualTo( expectedSemanticIds );
      assertThat( actual.visibleSpecificAssetIdNames() ).isEqualTo( expectedSpecificAssetIdNames );
   }
}
