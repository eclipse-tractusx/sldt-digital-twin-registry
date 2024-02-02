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
package org.eclipse.tractusx.semantics.registry;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

      //TODO: Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1
      // @Test
      public void testRbacCreateShellInBatch() throws Exception {
         super.testRbacCreateShellInBatch();
      }

      //        @Test - don't have /fetch
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

      //TODO: Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1
      //@Test
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
         //TODO: enable when we are no longer using the file based access rules
         // This test is using a random prefix in the the name of the specificAssetId that has to match
         //super.testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityAndWildcardExpectSuccess();
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
         //TODO: enable when public access is implemented
         //super.testGetAllShellsWithPublicAccessByTenantId();
      }

      @Test
      public void testGetShellByExternalIdByOwningTenantId() throws Exception {
         super.testGetShellByExternalIdByOwningTenantId();
      }

      @Test
      public void testGetAllShellByExternalIdWithPublicAccessByTenantId() throws Exception {
         //TODO: enable when public access is implemented
         //super.testGetAllShellByExternalIdWithPublicAccessByTenantId();
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
}
