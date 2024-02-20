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

import static org.eclipse.tractusx.semantics.registry.TestUtil.getEncodedValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.aas.registry.model.SubmodelDescriptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( profiles = { "granular", "test" } )
@EnableConfigurationProperties( RegistryProperties.class )
public class GranularAssetAdministrationShellApiSecurityTest extends AssetAdministrationShellApiSecurityTest {

   private static final String HTTP_EDC_DATA_PLANE_URL = "{\"submodelEndpointUrl\": \"http://edc-data-plane/url\"}";
   private static final String EXISTING_URL = "{\"submodelEndpointUrl\": \"http://endpoint-address\"}";

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
      @Disabled("Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1")
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

      @Test
      public void testGetAllShellsWithDefaultClosedFilteredSpecificAssetIdsByTenantId() throws Exception {
         super.testGetAllShellsWithDefaultClosedFilteredSpecificAssetIdsByTenantId();
      }

      @Test
      public void testGetShellWithFilteredSpecificAssetIdsByTenantId() throws Exception {
         super.testGetShellWithFilteredSpecificAssetIdsByTenantId();
      }

      @Test
      @Disabled("Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1")
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

      @Test
      public void testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityAndWildcardExpectSuccess() throws Exception {
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

      @Test
      public void testGetAllShellsByOwningTenantId() throws Exception {
         super.testGetAllShellsByOwningTenantId();
      }

      @Test
      public void testGetAllShellsWithPublicAccessByTenantId() throws Exception {
         super.testGetAllShellsWithPublicAccessByTenantId();
      }

      @Test
      public void testGetShellByExternalIdByOwningTenantId() throws Exception {
         super.testGetShellByExternalIdByOwningTenantId();
      }

      @Test
      public void testGetAllShellByExternalIdWithPublicAccessByTenantId() throws Exception {
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

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutTokenExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( HTTP_EDC_DATA_PLANE_URL )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isUnauthorized() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutAppropriateRoleExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.readTwin() )
                           .content( HTTP_EDC_DATA_PLANE_URL )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutContentExpectBadRequest() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
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
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantOne().submodelAccessControl() )
                           .content( HTTP_EDC_DATA_PLANE_URL )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithoutAnyShellsExpectForbidden() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantOne().submodelAccessControl() )
                           .content( HTTP_EDC_DATA_PLANE_URL )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      @Disabled( "disabled while we have no way to create dynamic rules" )
      void testPostSubmodelDescriptorAuthorizedWithoutMatchingSemanticIdExpectForbidden() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor( UUID.randomUUID().toString(), "http://endpoint-address" );
         shellPayload.setId( UUID.randomUUID().toString() );

         SpecificAssetId asset = TestUtil.createSpecificAssetId( "tenantTwo", "value_2_private", List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         shellPayload.setSpecificAssetIds( List.of( asset ) );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         //Tenant two should not have access due to the random specificAssetId
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantTwo().submodelAccessControl() )
                           .content( EXISTING_URL )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantTwo().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }

      @Test
      void testPostSubmodelDescriptorAuthorizedWithMatchingShellAndSemanticIdExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setSpecificAssetIds( null );
         shellPayload.setId( UUID.randomUUID().toString() );

         SpecificAssetId asset = TestUtil.createSpecificAssetId( "tenantTwo", "value_2_private", List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         shellPayload.setSpecificAssetIds( List.of( asset ) );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         SubmodelDescriptor submodel = TestUtil.createSubmodel();
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ), getEncodedValue( shellPayload.getId() ) );

         //Tenant two should have access due to the default semantic Id value
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantTwo().submodelAccessControl() )
                           .content( EXISTING_URL )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantTwo().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
      }

      @Test
      @Disabled( "disabled while we have no way to create dynamic rules" )
      void testPostSubmodelDescriptorAuthorizedWithoutMatchingShellExpectForbidden() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setSpecificAssetIds( null );
         shellPayload.setId( UUID.randomUUID().toString() );

         SpecificAssetId asset = TestUtil.createSpecificAssetId( "tenantTwo", "value_2_private", List.of( jwtTokenFactory.tenantTwo().getTenantId() ) );
         shellPayload.setSpecificAssetIds( List.of( asset ) );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         SubmodelDescriptor submodel = TestUtil.createSubmodel();
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ), getEncodedValue( shellPayload.getId() ) );

         //Tenant three should have access due to the non-visible shell
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( "/api/v3.0/submodel-descriptor/authorized" )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.tenantThree().submodelAccessControl() )
                           .content( EXISTING_URL )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantThree().getTenantId() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isForbidden() );
      }
   }
}
