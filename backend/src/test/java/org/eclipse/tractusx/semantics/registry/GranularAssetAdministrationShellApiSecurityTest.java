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

package org.eclipse.tractusx.semantics.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;
import org.eclipse.tractusx.semantics.accesscontrol.sql.repository.AccessControlRuleRepository;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.AasPolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.AccessRuleValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.AccessRuleValues;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.CreateAccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.OperatorType;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.PolicyType;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.ReadUpdateAccessRule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( profiles = { "granular", "test" } )
@EnableConfigurationProperties( RegistryProperties.class )
public class GranularAssetAdministrationShellApiSecurityTest extends AssetAdministrationShellApiSecurityTest {

   @Nested
   @DisplayName( "Authentication Tests" )
   class SecurityTests extends AssetAdministrationShellApiSecurityTest.SecurityTests {
      @Test
      public void testWithoutAuthenticationTokenProvidedExpectUnauthorized() throws Exception {
         super.testWithoutAuthenticationTokenProvidedExpectUnauthorized();
      }

      @Test
      public void testWithAuthenticationTokenProvidedExpectUnauthorized() throws Exception {
         super.testWithAuthenticationTokenProvidedExpectUnauthorized();
      }

      @Test
      public void testWithInvalidAuthenticationTokenConfigurationExpectUnauthorized() throws Exception {
         super.testWithInvalidAuthenticationTokenConfigurationExpectUnauthorized();
      }
   }

   @Nested
   @DisplayName( "Shell Authorization Test" )
   class ShellCrudTest extends AssetAdministrationShellApiSecurityTest.ShellCrudTest {

      @Test
      public void testRbacForGetAll() throws Exception {
         super.testRbacForGetAll();
      }

      @Test
      public void testRbacForGetById() throws Exception {
         super.testRbacForGetById();
      }

      @Test
      public void testRbacForCreate() throws Exception {
         super.testRbacForCreate();
      }

      @Test
      public void testRbacForUpdate() throws Exception {
         super.testRbacForUpdate();
      }

      @Test
      public void testRbacForDelete() throws Exception {
         super.testRbacForDelete();
      }
   }

   @Nested
   @DisplayName( "Submodel Descriptor Authorization Test" )
   class SubmodelDescriptorCrudTests extends AssetAdministrationShellApiSecurityTest.SubmodelDescriptorCrudTests {

      @Test
      public void testRbacForGetAll() throws Exception {
         super.testRbacForGetAll();
      }

      @Test
      public void testRbacForGetById() throws Exception {
         super.testRbacForGetById();
      }

      @Test
      public void testRbacForCreate() throws Exception {
         super.testRbacForCreate();
      }

      @Test
      public void testRbacForUpdate() throws Exception {
         super.testRbacForUpdate();
      }

      @Test
      public void testRbacForDelete() throws Exception {
         super.testRbacForDelete();
      }
   }

   @Nested
   @DisplayName( "SpecificAssetIds Crud Test" )
   class SpecificAssetIdsCrudTest extends AssetAdministrationShellApiSecurityTest.SpecificAssetIdsCrudTest {

      @Test
      public void testRbacForGet() throws Exception {
         super.testRbacForGet();
      }

      @Test
      public void testRbacForCreate() throws Exception {
         super.testRbacForCreate();
      }

      @Test
      public void testRbacForDelete() throws Exception {
         super.testRbacForDelete();
      }
   }

   @Nested
   @DisplayName( "Lookup Authorization Test" )
   class LookupTest extends AssetAdministrationShellApiSecurityTest.LookupTest {

      @Test
      public void testRbacForLookupByAssetIds() throws Exception {
         super.testRbacForLookupByAssetIds();
      }
   }

   @Nested
   @DisplayName( "Custom AAS API Authorization Tests" )
   class CustomAASApiTest extends AssetAdministrationShellApiSecurityTest.CustomAASApiTest {

      @Test
      @Disabled( "Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1" )
      public void testRbacCreateShellInBatch() throws Exception {
         super.testRbacCreateShellInBatch();
      }

      @Test
      @Disabled( "Don't have /fetch" )
      public void testRbacForFetchShellsByIds() throws Exception {
         super.testRbacForFetchShellsByIds();
      }
   }

   @Nested
   @DisplayName( "Tenant based specificAssetId visibility test" )
   class TenantBasedVisibilityTest extends AssetAdministrationShellApiSecurityTest.TenantBasedVisibilityTest {

      @Autowired
      private AccessControlRuleRepository accessControlRuleRepository;

      @Test
      public void testGetAllShellsWithDefaultClosedFilteredSpecificAssetIdsByTenantId() throws Exception {
         super.testGetAllShellsWithDefaultClosedFilteredSpecificAssetIdsByTenantId();
      }

      @Test
      @DisplayName( "Test GetShell with filtered specificAssetId by tenantId" )
      public void testGetShellWithFilteredSpecificAssetIdsByTenantId() throws Exception {
         // Create and save rule
         accessControlRuleRepository.saveAllAndFlush( List.of(
               // Rule for BPN
               TestUtil.createAccessRule(
                     // Rule for BPN
                     TestUtil.PUBLIC_READABLE,
                     // Rule for mandatory specificAssetIds
                     Map.of( "manufacturerPartId", keyPrefix + "wildcardAllowed" ),
                     // Rule for visible specificAssetIds
                     Set.of( "manufacturerPartId" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule(
                     // Rule for BPN
                     jwtTokenFactory.tenantTwo().getTenantId(),
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue", keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue" ),
                     // Rule for visible specificAssetIds
                     Set.of( keyPrefix + "CustomerPartId", keyPrefix + "MaterialNumber" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule(
                     // Rule for BPN
                     jwtTokenFactory.tenantThree().getTenantId(),
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "CustomerPartId2", "tenantThreeAssetIdValue" ),
                     // Rule for visible specificAssetIds
                     Set.of( keyPrefix + "CustomerPartId2" ), Set.of( keyPrefix + "semanticId" ) )
         ) );

         SpecificAssetId asset1 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset2 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId2", "tenantThreeAssetIdValue",
               List.of( jwtTokenFactory.tenantThree().getTenantId() ) );
         SpecificAssetId asset3 = TestUtil.createSpecificAssetId( keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         // Define specificAsset with wildcard which not allowed. (Only manufacturerPartId is defined in application.yml)
         SpecificAssetId asset4 = TestUtil.createSpecificAssetId( keyPrefix + "BPID", "ignoreWildcard", List.of( getExternalSubjectIdWildcardPrefix() ) );
         // Define specificAsset with wildcard which is allowed. (Only manufacturerPartId is defined in application.yml)
         SpecificAssetId asset5 = TestUtil.createSpecificAssetId( "manufacturerPartId", keyPrefix + "wildcardAllowed",
               List.of( getExternalSubjectIdWildcardPrefix() ) );

         // Define all available specificAssetIds
         List<SpecificAssetId> specificAssetIds = List.of( asset1, asset2, asset3, asset4, asset5 );
         // Define available specificAssetIds for tenantTwo
         List<SpecificAssetId> expectedSpecificAssetIdsTenantTwo = List.of( asset1, asset3, asset5 );
         super.testGetShellWithFilteredSpecificAssetIdsByTenantId( specificAssetIds, expectedSpecificAssetIdsTenantTwo );
      }

      @Test
      @DisplayName( "Test GetShell with filtered specificAssetIds with same keys where the specific mandatory Rule match over ANY rule by tenantId" )
      public void testGetShellWithFilteredSpecificAssetIdsWithSameKeysAndMandatoryRuleMatchedOverAnyByTenantId() throws Exception {
         // Create and save rule
         accessControlRuleRepository.saveAllAndFlush( List.of(
               // Rule for BPN
               TestUtil.createAccessRule(
                     // Rule for BPN
                     TestUtil.PUBLIC_READABLE,
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "BPID", "ignoreWildcard", "manufacturerPartId", keyPrefix + "wildcardAllowed" ),
                     // Rule for visible specificAssetIds
                     Set.of( "manufacturerPartId" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule(
                     // Rule for BPN
                     jwtTokenFactory.tenantTwo().getTenantId(),
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue", keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue" ),
                     // Rule for visible specificAssetIds
                     Set.of( keyPrefix + "CustomerPartId", keyPrefix + "MaterialNumber" ), Set.of( keyPrefix + "semanticId" ) ) ) );

         SpecificAssetId asset1 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset2 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantThreeAssetIdValue",
               List.of( jwtTokenFactory.tenantThree().getTenantId() ) );
         SpecificAssetId asset3 = TestUtil.createSpecificAssetId( keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );

         // Define all available specificAssetIds
         List<SpecificAssetId> specificAssetIds = List.of( asset1, asset2, asset3 );
         // Define available specificAssetIds for tenantTwo
         List<SpecificAssetId> expectedSpecificAssetIdsTenantTwo = List.of( asset1, asset3 );
         super.testGetShellWithFilteredSpecificAssetIdsByTenantId( specificAssetIds, expectedSpecificAssetIdsTenantTwo );
      }

      @Test
      @DisplayName( "Test GetShell with filtered specificAssetIds with same keys where the ANY Rule matched by tenantId" )
      public void testGetShellWithFilteredSpecificAssetIdsWithSameKeysAndAnyRuleMatchedByTenantId() throws Exception {
         // Create and save rule
         accessControlRuleRepository.saveAllAndFlush( List.of(
               TestUtil.createAccessRule(
                     // Rule for BPN
                     TestUtil.PUBLIC_READABLE,
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "BPID", "ignoreWildcard", "manufacturerPartId", keyPrefix + "wildcardAllowed" ),
                     // Rule for visible specificAssetIds
                     Set.of( "manufacturerPartId" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule(
                     // Rule for BPN
                     jwtTokenFactory.tenantTwo().getTenantId(),
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue" ),
                     // Rule for visible specificAssetIds
                     Set.of( keyPrefix + "CustomerPartId", keyPrefix + "MaterialNumber" ), Set.of( keyPrefix + "semanticId" ) ) ) );

         SpecificAssetId asset1 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset2 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantThreeAssetIdValue",
               List.of( jwtTokenFactory.tenantThree().getTenantId() ) );
         SpecificAssetId asset3 = TestUtil.createSpecificAssetId( keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset4 = TestUtil.createSpecificAssetId( keyPrefix + "PartInstanceId", "OwnerAssetidValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );

         // Define all available specificAssetIds
         List<SpecificAssetId> specificAssetIds = List.of( asset1, asset2, asset3, asset4 );
         // Define available specificAssetIds for tenantTwo
         List<SpecificAssetId> expectedSpecificAssetIdsTenantTwo = List.of( asset1, asset2, asset3 );
         super.testGetShellWithFilteredSpecificAssetIdsByTenantId( specificAssetIds, expectedSpecificAssetIdsTenantTwo );
      }

      @Test
      @DisplayName( "Test GetShell with filtered specificAssetIds with same keys where mandatory rule not match and disable specificAssetId by tenantId" )
      public void testGetShellWithFilteredSpecificAssetIdsWithSameKeysAndMandatoryRuleNotMatchedTenantId() throws Exception {
         // Create and save rule
         accessControlRuleRepository.saveAllAndFlush( List.of(
               TestUtil.createAccessRule(
                     // Rule for BPN
                     TestUtil.PUBLIC_READABLE,
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "BPID", "ignoreWildcard", "manufacturerPartId", keyPrefix + "wildcardAllowed" ),
                     // Rule for visible specificAssetIds
                     Set.of( "manufacturerPartId" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule(
                     // Rule for BPN
                     jwtTokenFactory.tenantTwo().getTenantId(),
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue" ),
                     // Rule for visible specificAssetIds
                     Set.of( keyPrefix + "CustomerPartId", keyPrefix + "MaterialNumber" ), Set.of( keyPrefix + "semanticId" ) ) ) );

         SpecificAssetId asset1 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset2 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantThreeAssetIdValue",
               List.of( jwtTokenFactory.tenantThree().getTenantId() ) );
         SpecificAssetId asset3 = TestUtil.createSpecificAssetId( keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset4 = TestUtil.createSpecificAssetId( keyPrefix + "PartInstanceId", "OwnerAssetidValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );

         // Define all available specificAssetIds
         List<SpecificAssetId> specificAssetIds = List.of( asset1, asset2, asset3, asset4 );
         // Define available specificAssetIds for tenantTwo
         List<SpecificAssetId> expectedSpecificAssetIdsTenantTwo = List.of( asset3 );
         super.testGetShellWithFilteredSpecificAssetIdsByTenantId( specificAssetIds, expectedSpecificAssetIdsTenantTwo );
      }

      @Test
      @DisplayName( "Test GetShell with filtered specificAssetIds with same keys and multiple rules where first rule with mandatory rule not match and the second rule matched with any by tenantId" )
      public void testGetShellWithFilteredSpecificAssetIdsWithSameKeysAndMultipleRulesTenantId() throws Exception {
         // Create and save rule
         accessControlRuleRepository.saveAllAndFlush( List.of(
               TestUtil.createAccessRule(
                     // Rule for BPN
                     jwtTokenFactory.tenantTwo().getTenantId(),
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "MaterialNumber", "notMatchedValue" ),
                     // Rule for visible specificAssetIds
                     Set.of( keyPrefix + "CustomerPartId", keyPrefix + "MaterialNumber" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule(
                     // Rule for BPN
                     jwtTokenFactory.tenantTwo().getTenantId(),
                     // Rule for mandatory specificAssetIds
                     Map.of( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue" ),
                     // Rule for visible specificAssetIds
                     Set.of( keyPrefix + "CustomerPartId", keyPrefix + "MaterialNumber" ), Set.of( keyPrefix + "semanticId" ) ) ) );

         SpecificAssetId asset1 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantTwoAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset2 = TestUtil.createSpecificAssetId( keyPrefix + "CustomerPartId", "tenantThreeAssetIdValue",
               List.of( jwtTokenFactory.tenantThree().getTenantId() ) );
         SpecificAssetId asset3 = TestUtil.createSpecificAssetId( keyPrefix + "MaterialNumber", "withoutTenantAssetIdValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         SpecificAssetId asset4 = TestUtil.createSpecificAssetId( keyPrefix + "PartInstanceId", "OwnerAssetidValue",
               List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );

         // Define all available specificAssetIds
         List<SpecificAssetId> specificAssetIds = List.of( asset1, asset2, asset3, asset4 );
         // Define available specificAssetIds for tenantTwo
         List<SpecificAssetId> expectedSpecificAssetIdsTenantTwo = List.of( asset1, asset3 );
         super.testGetShellWithFilteredSpecificAssetIdsByTenantId( specificAssetIds, expectedSpecificAssetIdsTenantTwo );
      }

      @Test
      @Disabled( "Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1" )
      public void testFetchShellsWithFilteredSpecificAssetIdsByTenantId() throws Exception {
         super.testFetchShellsWithFilteredSpecificAssetIdsByTenantId();
      }

      @Test
      public void testGetSpecificAssetIdsFilteredByTenantId() throws Exception {
         super.testGetSpecificAssetIdsFilteredByTenantId();
      }

      @Test
      public void testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityExpectSuccess() throws Exception {
         super.testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityExpectSuccess();
      }

      @Disabled("Deprecated API call: GET /lookup/shells")
      @Test
      public void testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityAndWildcardExpectSuccess() throws Exception {
         accessControlRuleRepository.saveAllAndFlush( List.of(
               TestUtil.createAccessRule( TestUtil.PUBLIC_READABLE,
                     Map.of( "manufacturerPartId", keyPrefix + "value_2" ),
                     Set.of( "manufacturerPartId" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule( jwtTokenFactory.tenantTwo().getTenantId(),
                     Map.of( keyPrefix + "tenantTwo_tenantThree", "value_3", keyPrefix + "tenantTwo", "value_2_private" ),
                     Set.of( keyPrefix + "tenantTwo_tenantThree", keyPrefix + "tenantTwo" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule( jwtTokenFactory.tenantThree().getTenantId(),
                     Map.of( keyPrefix + "tenantTwo_tenantThree", "value_3" ),
                     Set.of( keyPrefix + "tenantTwo_tenantThree" ), Set.of( keyPrefix + "semanticId" ) )
         ) );
         super.testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityAndWildcardExpectSuccess();
      }

      @Test
      public void testFindExternalShellIdsBySpecificAssetIdsWithDefaultClosedTenantBasedVisibilityExpectSuccess() throws Exception {
         super.testFindExternalShellIdsBySpecificAssetIdsWithDefaultClosedTenantBasedVisibilityExpectSuccess();
      }
   }

   @Nested
   @DisplayName( "Tenant based Shell visibility test" )
   class TenantBasedShellVisibilityTest extends AssetAdministrationShellApiSecurityTest.TenantBasedShellVisibilityTest {

      @Autowired
      private AccessControlRuleRepository accessControlRuleRepository;

      @Test
      public void testGetAllShellsByOwningTenantId() throws Exception {
         super.testGetAllShellsByOwningTenantId();
      }

      @Test
      public void testGetAllShellsWithPublicAccessByTenantId() throws Exception {
         accessControlRuleRepository.saveAllAndFlush( List.of(
               TestUtil.createAccessRule( TestUtil.PUBLIC_READABLE,
                     Map.of( "manufacturerPartId", keyPrefix + "value_2" ),
                     Set.of( "manufacturerPartId" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule( jwtTokenFactory.tenantTwo().getTenantId(),
                     Map.of( keyPrefix + "tenantTwo", "value_2_public" ),
                     Set.of( keyPrefix + "tenantTwo" ), Set.of( keyPrefix + "semanticId" ) )
         ) );
         super.testGetAllShellsWithPublicAccessByTenantId();
      }

      @Test
      public void testGetShellByExternalIdByOwningTenantId() throws Exception {
         super.testGetShellByExternalIdByOwningTenantId();
      }

      @Test
      public void testGetAllShellByExternalIdWithPublicAccessByTenantId() throws Exception {
         accessControlRuleRepository.saveAllAndFlush( List.of(
               TestUtil.createAccessRule( TestUtil.PUBLIC_READABLE,
                     Map.of( "manufacturerPartId", keyPrefix + "value_2" ),
                     Set.of( "manufacturerPartId" ), Set.of( keyPrefix + "semanticId" ) ),
               TestUtil.createAccessRule( jwtTokenFactory.tenantTwo().getTenantId(),
                     Map.of( keyPrefix + "tenantTwo", "value_2_public" ),
                     Set.of( keyPrefix + "tenantTwo" ), Set.of() )
         ) );
         super.testGetAllShellByExternalIdWithPublicAccessByTenantId();
      }
   }

   @Nested
   @DisplayName( "Description Authentication Tests" )
   class DescriptionApiTest extends AssetAdministrationShellApiSecurityTest.DescriptionApiTest {

      @Test
      public void testGetDescriptionOnlyDeleteRoleExpectForbidden() throws Exception {
         super.testGetDescriptionOnlyDeleteRoleExpectForbidden();
      }

      @Test
      public void testGetDescriptionNoRoleExpectForbidden() throws Exception {
         super.testGetDescriptionNoRoleExpectForbidden();
      }

      @Test
      public void testGetDescriptionReadRoleExpectSuccess() throws Exception {
         super.testGetDescriptionReadRoleExpectSuccess();
      }

      @Test
      public void testGetDescriptionReadRoleExpectUnauthorized() throws Exception {
         super.testGetDescriptionReadRoleExpectUnauthorized();
      }
   }

   @Nested
   @DisplayName( "Submodel endpoint authorization Tests" )
   class SubmodelEndpointAuthorizationApiTest {

      private static final String HTTP_EDC_DATA_PLANE_URL_REQUEST = "{\"submodelEndpointUrl\": \"http://edc-data-plane/url\"}";
      private static final String EXISTING_URL = "http://endpoint-address";
      private static final String EXISTING_URL_REQUEST_FORMAT = "{\"submodelEndpointUrl\": \"%s\"}";

      @Autowired
      private AccessControlRuleRepository accessControlRuleRepository;

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutTokenExpectUnauthorized() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( HTTP_EDC_DATA_PLANE_URL_REQUEST )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isUnauthorized() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutAppropriateRoleExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.readTwin() )
                           .content( HTTP_EDC_DATA_PLANE_URL_REQUEST )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutContentExpectBadRequest() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantOne().submodelAccessControl() )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isBadRequest() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutTenantIdExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantOne().submodelAccessControl() )
                           .content( HTTP_EDC_DATA_PLANE_URL_REQUEST )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutAnyShellsExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantOne().submodelAccessControl() )
                           .content( HTTP_EDC_DATA_PLANE_URL_REQUEST )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutMatchingSemanticIdExpectForbidden() throws Exception {
         String randomId = UUID.randomUUID().toString();
         AssetAdministrationShellDescriptor shellPayload = TestUtil
               .createCompleteAasDescriptor( randomId + "semanticIdExample", EXISTING_URL + randomId );
         shellPayload.setSpecificAssetIds( null );
         shellPayload.setId( randomId );

         String tenantTwoBpn = jwtTokenFactory.tenantTwo().getTenantId();
         SpecificAssetId asset = TestUtil.createSpecificAssetId( randomId + "tenantTwo", randomId + "value_2", List.of( tenantTwoBpn ) );
         shellPayload.setSpecificAssetIds( List.of( asset ) );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         final var accessRule = TestUtil.createAccessRule(
               tenantTwoBpn,
               Map.of( randomId + "tenantTwo", randomId + "value_2" ),
               Set.of( randomId + "tenantTwo" ),
               Set.of()
         );
         accessControlRuleRepository.saveAndFlush( accessRule );

         //Tenant two should not have access because the rule does not give access to any semanticIds
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantTwo().submodelAccessControl() )
                           .content( getRequestForUrl( EXISTING_URL + randomId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantTwo().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithMatchingShellAndSemanticIdExpectSuccess() throws Exception {
         String randomId = UUID.randomUUID().toString();
         AssetAdministrationShellDescriptor shellPayload = TestUtil
               .createCompleteAasDescriptor( randomId + "semanticIdExample", EXISTING_URL + randomId );
         shellPayload.setSpecificAssetIds( null );
         shellPayload.setId( randomId );

         String tenantTwoBpn = jwtTokenFactory.tenantTwo().getTenantId();
         SpecificAssetId asset = TestUtil.createSpecificAssetId( randomId + "tenantTwo", randomId + "value_2", List.of( tenantTwoBpn ) );
         shellPayload.setSpecificAssetIds( List.of( asset ) );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         final var accessRule = TestUtil.createAccessRule(
               tenantTwoBpn,
               Map.of( randomId + "tenantTwo", randomId + "value_2" ),
               Set.of( randomId + "tenantTwo" ),
               Set.of( randomId + "semanticIdExample" )
         );
         accessControlRuleRepository.saveAndFlush( accessRule );

         //Tenant two should have access due to the matching shell and semantic Id values
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantTwo().submodelAccessControl() )
                           .content( getRequestForUrl( EXISTING_URL + randomId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, tenantTwoBpn )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutMatchingShellExpectForbidden() throws Exception {
         String randomId = UUID.randomUUID().toString();
         AssetAdministrationShellDescriptor shellPayload = TestUtil
               .createCompleteAasDescriptor( randomId + "semanticIdExample", EXISTING_URL + randomId );
         shellPayload.setSpecificAssetIds( null );
         shellPayload.setId( randomId );

         String tenantTwoBpn = jwtTokenFactory.tenantTwo().getTenantId();
         SpecificAssetId asset = TestUtil.createSpecificAssetId( randomId + "tenantTwo", randomId + "value_2", List.of( tenantTwoBpn ) );
         shellPayload.setSpecificAssetIds( List.of( asset ) );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         final var accessRule = TestUtil.createAccessRule(
               tenantTwoBpn,
               Map.of( randomId + "tenantTwo", randomId + "value_2" ),
               Set.of( randomId + "tenantTwo" ),
               Set.of( randomId + "semanticIdExample" )
         );
         accessControlRuleRepository.saveAndFlush( accessRule );

         //Tenant three should have access due to the non-visible shell (as it is only visible to tenantTwo
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantThree().submodelAccessControl() )
                           .content( getRequestForUrl( EXISTING_URL + randomId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantThree().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      private String getRequestForUrl( String url ) {
         return String.format( EXISTING_URL_REQUEST_FORMAT, url );
      }
   }

   @Nested
   @DisplayName( "Access rule endpoint Tests" )
   class AccessRuleEndpointApiTest {

      private static final String BPN = "BPN";
      private static final String MANDATORY_NAME = "mandatory-name";
      private static final String MANDATORY_VALUE = "mandatory-value";
      private static final String VISIBLE = "visible";
      private static final String SEMANTIC_ID = "semanticId";
      private static final OffsetDateTime DATE_TIME_FROM = OffsetDateTime.now( ZoneOffset.UTC )
            //Make sure the timestamp is never 00 seconds to avoid truncation when the expected value is calculated
            .truncatedTo( ChronoUnit.MINUTES )
            .truncatedTo( ChronoUnit.SECONDS ).plusSeconds( 12 );
      private static final OffsetDateTime DATE_TIME_TO = DATE_TIME_FROM.plusMinutes( 1L );

      private final AasPolicy defaultPolicy = getAasPolicy( BPN, Map.of( MANDATORY_NAME, MANDATORY_VALUE ), Set.of( VISIBLE ), Set.of( SEMANTIC_ID ) );
      @Autowired
      private AccessControlRuleRepository accessControlRuleRepository;
      @Autowired
      private ObjectMapper objectMapper;

      @Test
      void testGetAccessRulesWithoutTokenExpectUnauthorized() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules" )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isUnauthorized() );
      }

      @Test
      void testPostAccessRuleWithoutTokenExpectUnauthorized() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/access-controls/rules" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( objectMapper.writeValueAsString( new CreateAccessRule()
                                 .policyType( PolicyType.AAS )
                                 .policy( defaultPolicy )
                                 .description( UUID.randomUUID().toString() ) ) )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isUnauthorized() );
      }

      @Test
      void testGetAnAccessRuleWithoutTokenExpectUnauthorized() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules/1" )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isUnauthorized() );
      }

      @Test
      void testPutAnAccessRuleWithoutTokenExpectUnauthorized() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .put( "/api/v3/access-controls/rules/1" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( objectMapper.writeValueAsString( new ReadUpdateAccessRule()
                                 .id( 1L )
                                 .tid( jwtTokenFactory.tenantOne().getTenantId() )
                                 .policyType( PolicyType.AAS )
                                 .policy( defaultPolicy )
                                 .description( UUID.randomUUID().toString() ) ) )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isUnauthorized() );
      }

      @Test
      void testDeleteAnAccessRuleWithoutTokenExpectUnauthorized() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( "/api/v3/access-controls/rules/1" )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isUnauthorized() );
      }

      @Test
      void testGetAccessRulesWithWrongTokenExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules" )
                           .with( jwtTokenFactory.tenantOne().writeAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostAccessRuleWithWrongTokenExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/access-controls/rules" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( objectMapper.writeValueAsString( new CreateAccessRule()
                                 .policyType( PolicyType.AAS )
                                 .policy( defaultPolicy )
                                 .description( UUID.randomUUID().toString() ) ) )
                           .with( jwtTokenFactory.tenantOne().readAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testGetAnAccessRuleWithWrongTokenExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules/1" )
                           .with( jwtTokenFactory.tenantOne().writeAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPutAnAccessRuleWithWrongTokenExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .put( "/api/v3/access-controls/rules/1" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( objectMapper.writeValueAsString( new ReadUpdateAccessRule()
                                 .id( 1L )
                                 .tid( jwtTokenFactory.tenantOne().getTenantId() )
                                 .policyType( PolicyType.AAS )
                                 .policy( defaultPolicy )
                                 .description( UUID.randomUUID().toString() ) ) )
                           .with( jwtTokenFactory.tenantOne().readAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testDeleteAnAccessRuleWithWrongTokenExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( "/api/v3/access-controls/rules/1" )
                           .with( jwtTokenFactory.tenantOne().readAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testGetAccessRulesWithTokenExpectSuccess() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules" )
                           .with( jwtTokenFactory.tenantOne().readAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.items" ).exists() );
      }

      @Test
      void testPostAccessRuleWithTokenExpectSuccess() throws Exception {
         String description = UUID.randomUUID().toString();
         String responseBody = mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3/access-controls/rules" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( objectMapper.writeValueAsString( new CreateAccessRule()
                                 .policyType( PolicyType.AAS )
                                 .policy( defaultPolicy )
                                 .description( description )
                                 .validFrom( DATE_TIME_FROM )
                                 .validTo( DATE_TIME_TO ) ) )
                           .with( jwtTokenFactory.tenantOne().writeAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isCreated() )
               .andExpect( jsonPath( "$.id" ).isNumber() )
               .andExpect( jsonPath( "$.tid" ).value( jwtTokenFactory.tenantOne().getTenantId() ) )
               .andExpect( jsonPath( "$.policyType" ).value( PolicyType.AAS.name() ) )
               .andExpect( jsonPath( "$.policy" ).exists() )
               .andExpect( jsonPath( "$.description" ).value( description ) )
               .andExpect( jsonPath( "$.validFrom" ).value( DATE_TIME_FROM.toString() ) )
               .andExpect( jsonPath( "$.validTo" ).value( DATE_TIME_TO.toString() ) )
               .andReturn()
               .getResponse()
               .getContentAsString();
         assertThat( objectMapper.readValue( responseBody, ReadUpdateAccessRule.class ).getPolicy() ).isEqualTo( defaultPolicy );
      }

      @Test
      void testGetAnAccessRuleWithTokenExpectSuccess() throws Exception {
         String description = UUID.randomUUID().toString();
         AccessRule saved = saveDefaultRule( description );
         String responseBody = mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules/" + saved.getId() )
                           .with( jwtTokenFactory.tenantOne().readAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.id" ).value( saved.getId() ) )
               .andExpect( jsonPath( "$.tid" ).value( jwtTokenFactory.tenantOne().getTenantId() ) )
               .andExpect( jsonPath( "$.policyType" ).value( PolicyType.AAS.name() ) )
               .andExpect( jsonPath( "$.policy" ).exists() )
               .andExpect( jsonPath( "$.description" ).value( description ) )
               .andExpect( jsonPath( "$.validFrom" ).value( DATE_TIME_FROM.toString() ) )
               .andExpect( jsonPath( "$.validTo" ).value( DATE_TIME_TO.toString() ) )
               .andReturn()
               .getResponse()
               .getContentAsString();
         assertThat( objectMapper.readValue( responseBody, ReadUpdateAccessRule.class ).getPolicy() ).isEqualTo( defaultPolicy );
      }

      @Test
      void testPutAnAccessRuleWithTokenExpectSuccess() throws Exception {
         AccessRulePolicy policy = new AccessRulePolicy();
         policy.setAccessRules( new LinkedHashSet<>() );
         AccessRule accessRule = new AccessRule();
         accessRule.setTid( "tid" );
         accessRule.setPolicyType( AccessRule.PolicyType.AAS );
         accessRule.setPolicy( policy );
         accessRule.setTargetTenant( "target" );
         AccessRule saved = accessControlRuleRepository.saveAndFlush( accessRule );

         String description = UUID.randomUUID().toString();
         String responseBody = mvc.perform(
                     MockMvcRequestBuilders
                           .put( "/api/v3/access-controls/rules/" + saved.getId() )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( objectMapper.writeValueAsString( new ReadUpdateAccessRule()
                                 .id( saved.getId() )
                                 .tid( jwtTokenFactory.tenantOne().getTenantId() )
                                 .policyType( PolicyType.AAS )
                                 .policy( defaultPolicy )
                                 .description( description )
                                 .validFrom( DATE_TIME_FROM )
                                 .validTo( DATE_TIME_TO ) ) )
                           .with( jwtTokenFactory.tenantOne().writeAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.id" ).value( saved.getId() ) )
               .andExpect( jsonPath( "$.tid" ).value( jwtTokenFactory.tenantOne().getTenantId() ) )
               .andExpect( jsonPath( "$.policyType" ).value( PolicyType.AAS.name() ) )
               .andExpect( jsonPath( "$.policy" ).exists() )
               .andExpect( jsonPath( "$.description" ).value( description ) )
               .andExpect( jsonPath( "$.validFrom" ).value( DATE_TIME_FROM.toString() ) )
               .andExpect( jsonPath( "$.validTo" ).value( DATE_TIME_TO.toString() ) )
               .andReturn()
               .getResponse()
               .getContentAsString();
         assertThat( objectMapper.readValue( responseBody, ReadUpdateAccessRule.class ).getPolicy() ).isEqualTo( defaultPolicy );
      }

      @Test
      void testDeleteAnAccessRuleWithTokenExpectSuccess() throws Exception {
         AccessRule saved = saveDefaultRule( UUID.randomUUID().toString() );
         //verify that it exists
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules/" + saved.getId() )
                           .with( jwtTokenFactory.tenantOne().readAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
         //delete
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( "/api/v3/access-controls/rules/" + saved.getId() )
                           .with( jwtTokenFactory.tenantOne().writeAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );
         //verify that it does not exist
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3/access-controls/rules/" + saved.getId() )
                           .with( jwtTokenFactory.tenantOne().readAccessRules() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() );
      }

      private AccessRule saveDefaultRule( String description ) {
         AccessRulePolicy policy = new AccessRulePolicy();
         policy.setAccessRules( new LinkedHashSet<>( List.of(
               new AccessRulePolicyValue( AccessRulePolicy.BPN_RULE_NAME, PolicyOperator.EQUALS, BPN, null ),
               new AccessRulePolicyValue( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of(
                     new AccessRulePolicyValue( MANDATORY_NAME, PolicyOperator.EQUALS, MANDATORY_VALUE, null )
               ) ),
               new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of(
                     new AccessRulePolicyValue( "name", PolicyOperator.EQUALS, VISIBLE, null )
               ) ),
               new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SEMANTIC_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of(
                     new AccessRulePolicyValue( "modelUrn", PolicyOperator.EQUALS, SEMANTIC_ID, null )
               ) )
         ) ) );
         AccessRule accessRule = new AccessRule();
         accessRule.setTid( jwtTokenFactory.tenantOne().getTenantId() );
         accessRule.setPolicyType( AccessRule.PolicyType.AAS );
         accessRule.setPolicy( policy );
         accessRule.setTargetTenant( jwtTokenFactory.tenantTwo().getTenantId() );
         accessRule.setDescription( description );
         accessRule.setValidFrom( DATE_TIME_FROM.toInstant() );
         accessRule.setValidTo( DATE_TIME_TO.toInstant() );
         return accessControlRuleRepository.saveAndFlush( accessRule );
      }

      private AasPolicy getAasPolicy( String bpn, Map<String, String> mandatoryName, Set<String> visible, Set<String> semanticIds ) {
         final Set<AccessRuleValues> rules = new LinkedHashSet<>();
         rules.add( new AccessRuleValues()
               .attribute( AccessRulePolicy.BPN_RULE_NAME )
               .operator( OperatorType.EQ )
               .value( bpn ) );
         rules.add( new AccessRuleValues()
               .attribute( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME )
               .operator( OperatorType.INCLUDES )
               .values( mandatoryName.entrySet().stream()
                     .map( entry -> new AccessRuleValue()
                           .attribute( entry.getKey() )
                           .operator( OperatorType.EQ )
                           .value( entry.getValue() ) )
                     .collect( Collectors.toSet() ) ) );
         rules.add( new AccessRuleValues()
               .attribute( AccessRulePolicy.VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME )
               .operator( OperatorType.INCLUDES )
               .values( visible.stream()
                     .map( item -> new AccessRuleValue()
                           .attribute( "name" )
                           .operator( OperatorType.EQ )
                           .value( item ) )
                     .collect( Collectors.toSet() ) ) );
         rules.add( new AccessRuleValues()
               .attribute( AccessRulePolicy.VISIBLE_SEMANTIC_IDS_RULE_NAME )
               .operator( OperatorType.INCLUDES )
               .values( semanticIds.stream()
                     .map( item -> new AccessRuleValue()
                           .attribute( "modelUrn" )
                           .operator( OperatorType.EQ )
                           .value( item ) )
                     .collect( Collectors.toSet() ) ) );
         return new AasPolicy().accessRules( rules );
      }
   }
}
