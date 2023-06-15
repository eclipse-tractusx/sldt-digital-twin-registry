/********************************************************************************
 * Copyright (c) 2021-2023 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2021-2023 Contributors to the Eclipse Foundation
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
 ********************************************************************************/
package org.eclipse.tractusx.semantics.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.tractusx.semantics.aas.registry.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *  This class contains test to verify Authentication and RBAC based Authorization for all API endpoints.
 *  Every API endpoint is tested explicitly.
 */
public class AssetAdministrationShellApiSecurityTest extends AbstractAssetAdministrationShellApi {

    @Nested
    @DisplayName("Authentication Tests")
    class SecurityTests {
        @Test
        public void testWithoutAuthenticationTokenProvidedExpectUnauthorized() throws Exception {
                       mvc.perform(
                            MockMvcRequestBuilders
                                  .get(SINGLE_SHELL_BASE_PATH, UUID.randomUUID())
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        public void testWithAuthenticationTokenProvidedExpectUnauthorized() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, UUID.randomUUID())
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        public void testWithInvalidAuthenticationTokenConfigurationExpectUnauthorized() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, UUID.randomUUID())
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.withoutResourceAccess())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, UUID.randomUUID())
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.withoutRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("Shell Authorization Test")
    class ShellCrudTest {
        String shellId;

        @BeforeEach
        public void before() throws Exception{
            AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
            shellPayload1.setId(UUID.randomUUID().toString());
            performShellCreateRequest(mapper.writeValueAsString(shellPayload1));
            shellId = shellPayload1.getId();

        }

        @Test
        public void testRbacForGetAll() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SHELL_BASE_PATH)
                                    .accept(MediaType.APPLICATION_JSON)
                                    // test with wrong role
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SHELL_BASE_PATH)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

        @Test
        public void testRbacForGetById() throws Exception {
            // get shell by id
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, shellId )
                                    .accept(MediaType.APPLICATION_JSON)
                                    // test with wrong role
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, shellId )
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

        @Test
        public void testRbacForCreate() throws Exception {
            AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
            shellPayload1.setId(UUID.randomUUID().toString());
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(shellPayload1))
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            shellPayload1.setId(UUID.randomUUID().toString());
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH, mapper.writeValueAsString(shellPayload1) )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(shellPayload1))
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated());
        }

        @Test
        public void testRbacForUpdate() throws Exception {

           AssetAdministrationShellDescriptor testAas = TestUtil.createCompleteAasDescriptor();
           testAas.setId( shellId );

           String shellPayloadForUpdate = mapper.writeValueAsString(testAas);
            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SHELL_BASE_PATH, shellId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(shellPayloadForUpdate)
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());


            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SHELL_BASE_PATH, shellId )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(shellPayloadForUpdate)
                                    .with(jwtTokenFactory.updateTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent());

        }

        @Test
        public void testRbacForDelete() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_SHELL_BASE_PATH, shellId )
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_SHELL_BASE_PATH, shellId )
                                    // test with wrong role
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent());
        }

    }

    @Nested
    @DisplayName("Submodel Descriptor Authorization Test")
    class SubmodelDescriptorCrudTests {
       private String shellId;
       private String submodelId;
       private String submodelIdAas;

       @BeforeEach
       public void before() {

       }


       @Test
       public void testRbacForGetAll() throws Exception {
           AssetAdministrationShellDescriptor testAas = TestUtil.createCompleteAasDescriptor();
           testAas.setId(UUID.randomUUID().toString());
           performShellCreateRequest(mapper.writeValueAsString(testAas));
           shellId = testAas.getId();

           mvc.perform(
                           MockMvcRequestBuilders
                                   .get(SUB_MODEL_BASE_PATH,  shellId )
                                   .accept(MediaType.APPLICATION_JSON)
                                   // test with wrong role
                                   .with(jwtTokenFactory.addTwin())
                   )
                   .andDo(MockMvcResultHandlers.print())
                   .andExpect(status().isForbidden());

           mvc.perform(
                           MockMvcRequestBuilders
                                   .get(SUB_MODEL_BASE_PATH,  shellId )
                                   .accept(MediaType.APPLICATION_JSON)
                                   .with(jwtTokenFactory.readTwin())
                   )
                   .andDo(MockMvcResultHandlers.print())
                   .andExpect(status().isOk());
       }

        @Test
        public void testRbacForGetById() throws Exception {

           AssetAdministrationShellDescriptor testAas = TestUtil.createCompleteAasDescriptor();
            testAas.setId(UUID.randomUUID().toString());
          performShellCreateRequest(mapper.writeValueAsString(testAas));
          shellId = testAas.getId();
            submodelIdAas = testAas.getSubmodelDescriptors().get( 0 ).getId();

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelIdAas )
                                    .accept(MediaType.APPLICATION_JSON)
                                    // test with wrong role
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelIdAas )
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

        @Test
        public void testRbacForCreate() throws Exception {

           SubmodelDescriptor testSubmodelDescriptor = TestUtil.createSubmodel();
           testSubmodelDescriptor.setId( UUID.randomUUID().toString() );
           String submodelPayloadForCreate = mapper.writeValueAsString(testSubmodelDescriptor);

            AssetAdministrationShellDescriptor testAas = TestUtil.createCompleteAasDescriptor();
            testAas.setId(UUID.randomUUID().toString() );
            performShellCreateRequest(mapper.writeValueAsString(testAas));
            shellId = testAas.getId();


            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SUB_MODEL_BASE_PATH, shellId )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(submodelPayloadForCreate )
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            testSubmodelDescriptor.setId(UUID.randomUUID().toString());


            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SUB_MODEL_BASE_PATH, shellId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content( submodelPayloadForCreate )
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated());

        }

        @Test
        public void testRbacForUpdate() throws Exception {

            AssetAdministrationShellDescriptor testAas = TestUtil.createCompleteAasDescriptor();
            testAas.setId(UUID.randomUUID().toString() );
            performShellCreateRequest(mapper.writeValueAsString(testAas));
            shellId = testAas.getId();
            submodelIdAas = testAas.getSubmodelDescriptors().get( 0 ).getId();

           SubmodelDescriptor testSubmodelDescriptor = TestUtil.createSubmodel();
           testSubmodelDescriptor.setId(   submodelIdAas );



           String submodelPayloadForCreate = mapper.writeValueAsString(testSubmodelDescriptor);

            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(submodelPayloadForCreate)
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelIdAas)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(submodelPayloadForCreate)
                                    .with(jwtTokenFactory.updateTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent());
        }

        @Test
        public void testRbacForDelete() throws Exception {
            AssetAdministrationShellDescriptor testAas = TestUtil.createCompleteAasDescriptor();
            testAas.setId(UUID.randomUUID().toString() );
            performShellCreateRequest(mapper.writeValueAsString(testAas));
            shellId = testAas.getId();
            submodelIdAas = testAas.getSubmodelDescriptors().get( 0 ).getId();
            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelIdAas)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelIdAas)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent());
        }

    }

    @Nested
    @DisplayName("SpecificAssetIds Crud Test")
    class SpecificAssetIdsCrudTest {
        String shellId;

        @BeforeEach
        public void before() throws Exception{
            AssetAdministrationShellDescriptor testAas = TestUtil.createCompleteAasDescriptor();
            testAas.setId(UUID.randomUUID().toString());
            performShellCreateRequest(mapper.writeValueAsString(testAas));

            shellId = testAas.getId();
        }

        @Test
        public void testRbacForGet() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

        @Test
        public void testRbacForCreate() throws Exception {
            ArrayNode specificAssetIds = emptyArrayNode()
                    .add(specificAssetId("key1", "value1"))
                    .add(specificAssetId("key2", "value2"));

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(specificAssetIds))
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(specificAssetIds))
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated());
        }

        @Test
        public void testRbacForDelete() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Lookup Authorization Test")
    class LookupTest {

        @Test
        public void testRbacForLookupByAssetIds() throws Exception {

            SpecificAssetId specificAssetId = TestUtil.createSpecificAssetId();

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds", mapper.writeValueAsString(specificAssetId))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());
    mvc.perform(
                    MockMvcRequestBuilders
                            .get(LOOKUP_SHELL_BASE_PATH)
                            .param("assetIds",mapper.writeValueAsString(specificAssetId))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .queryParam("limit",  "10")
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .with(jwtTokenFactory.readTwin())
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());
        }

    }

    @Nested
    @DisplayName("Custom AAS API Authorization Tests")
    class CustomAASApiTest {

        //TODO: Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1
        // @Test
        public void testRbacCreateShellInBatch() throws Exception {
            ObjectNode shell = createShell();
            ArrayNode batchShellBody = emptyArrayNode().add(shell);

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH + "/batch")
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(batchShellBody))
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH + "/batch")
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(batchShellBody))
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated());
        }

       //TODO: Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1
       //@Test
        public void testRbacForFindShellsWithAnyMatch() throws Exception {
            JsonNode anyMatchLookupPayload = mapper.createObjectNode().set("query", mapper.createObjectNode()
                    .set("assetIds", emptyArrayNode().add(specificAssetId("abc", "123")))
            );
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(LOOKUP_SHELL_BASE_PATH + "/query")
                                    .content(toJson(anyMatchLookupPayload))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(LOOKUP_SHELL_BASE_PATH + "/query")
                                    .content(toJson(anyMatchLookupPayload))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

//        @Test - don't have /fetch
        public void testRbacForFetchShellsByIds() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH + "/fetch")
                                    .content(toJson(emptyArrayNode()))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH + "/fetch")
                                    .content(toJson(emptyArrayNode()))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)));
        }
    }

    /**
     * The specificAssetId#externalSubjectId indicates which tenant is allowed to see the specificAssetId and
     * find a Shell.
     *
     * Given:
     *  - Company A creates an AAS with multiple with: 1. one specificAssetId without externalSubjectId,
     *                                                 2. one with externalSubjectId = Company B
     *                                                 3. one with externalSubjectId = Company C
     *
     *   - Rules: When Company A requests the AAS, all specificAssetIds 1,2 and are shown. Company A is the owner of the AAS.
     *               The AAS Registry has an environment property "owningTenantId" that is compared with the tenantId from the token.
     *            When Company B requests the AAS, only specificAssetIds 1 and 2 are shown.
     *            When Company C requests the AAS, only specificAssetIds 1 and 3 are shown.
     *
     *            The same logic applies also to the lookup endpoints.
     *
     */
    @Nested
    @DisplayName("Tenant based specificAssetId visibility test")
    class TenantBasedVisibilityTest {

        @Test
        public void testGetAllShellsWithFilteredSpecificAssetIdsByTenantId() throws Exception {

            AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
            shellPayload.setId(UUID.randomUUID().toString());
            performShellCreateRequest(mapper.writeValueAsString(shellPayload));


            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SHELL_BASE_PATH)
                                    .queryParam("pageSize", "100")
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").exists());

            // test with tenant two
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SHELL_BASE_PATH)
                                    .queryParam("pageSize", "100")
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result[*].specificAssetIds[*].value", hasItems("identifier1ValueExample", "identifier2ValueExample")))
                    .andExpect(jsonPath("$.result[*].specificAssetIds[*].value", not(hasItem("tenantThreeAssetIdValue"))));
        }

        @Test
        public void testGetShellWithFilteredSpecificAssetIdsByTenantId() throws Exception {

            AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
            shellPayload.setSpecificAssetIds(null);
            SpecificAssetId asset1 = TestUtil.createSpecificAssetId("CustomerPartId","tenantTwoAssetIdValue",jwtTokenFactory.tenantTwo().getTenantId());
            SpecificAssetId asset2 = TestUtil.createSpecificAssetId("CustomerPartId","tenantThreeAssetIdValue",jwtTokenFactory.tenantThree().getTenantId());
            SpecificAssetId asset3 = TestUtil.createSpecificAssetId("MaterialNumber","withoutTenantAssetIdValue",null);

            shellPayload.setSpecificAssetIds(List.of(asset1,asset2,asset3));


            shellPayload.setId(UUID.randomUUID().toString());
            performShellCreateRequest(mapper.writeValueAsString(shellPayload));

            String shellId = shellPayload.getId();
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(shellId)))
                    .andExpect(jsonPath("$.specificAssetIds[*].value", containsInAnyOrder("tenantTwoAssetIdValue","tenantThreeAssetIdValue", "withoutTenantAssetIdValue")));

            // test with tenant two
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(shellId)))
                    .andExpect(jsonPath("$.specificAssetIds[*].value", hasItems("tenantTwoAssetIdValue", "withoutTenantAssetIdValue")))
                    .andExpect(jsonPath("$.specificAssetIds[*].value", not(hasItem("tenantThreeAssetIdValue"))));
        }

         //TODO: Test will be ignored, because the new api does not provided batch, fetch and query. This will be come later in version 0.3.1
        //@Test
        public void testFetchShellsWithFilteredSpecificAssetIdsByTenantId() throws Exception {
            ObjectNode shellPayload = createBaseIdPayload("example", "example");
            String tenantTwoAssetIdValue = "tenantTwofgkj129293";
            String tenantThreeAssetIdValue = "tenantThree543412394";
            String withoutTenantAssetIdValue = "withoutTenant329347192jf18";
            shellPayload.set("specificAssetIds", emptyArrayNode()
                    .add(specificAssetId("CustomerPartId", tenantTwoAssetIdValue,  jwtTokenFactory.tenantTwo().getTenantId()))
                    .add(specificAssetId("CustomerPartId", tenantThreeAssetIdValue, jwtTokenFactory.tenantThree().getTenantId()))
                    .add(specificAssetId("MaterialNumber",withoutTenantAssetIdValue))
            );
            performShellCreateRequest(toJson(shellPayload));
            String shellId = getId(shellPayload);

            ArrayNode queryPayload = emptyArrayNode().add(shellId);
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH + "/fetch")
                                    .content(toJson(queryPayload))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[*].identification", hasItem(shellId)))
                    .andExpect(jsonPath("$.items[*].specificAssetIds[*].value", hasItems(tenantTwoAssetIdValue,tenantThreeAssetIdValue, withoutTenantAssetIdValue)));

            // test with tenant two
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH + "/fetch")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(queryPayload))
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[*].identification", hasItem(shellId)))
                    .andExpect(jsonPath("$.items[*].specificAssetIds[*].value", hasItems(tenantTwoAssetIdValue, withoutTenantAssetIdValue)))
                    .andExpect(jsonPath("$.items[*].specificAssetIds[*].value", not(hasItem(tenantThreeAssetIdValue))));
        }



        @Test
        public void testGetSpecificAssetIdsFilteredByTenantId() throws Exception {

            AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
            shellPayload.setId(UUID.randomUUID().toString());
            performShellCreateRequest(mapper.writeValueAsString(shellPayload));

            SpecificAssetId specificAssetId = new SpecificAssetId();
            Reference externalSubjectId = new Reference();
            externalSubjectId.setType(ReferenceTypes.EXTERNALREFERENCE);
            Key key = new Key();
            key.setType(KeyTypes.SUBMODEL);
            key.setValue("semanticIdExample");
            externalSubjectId.setKeys(List.of(key));

            specificAssetId.setName("assetName");
            specificAssetId.setValue("assetValue");


            String shellId = shellPayload.getId();
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(List.of(specificAssetId)))
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(specificAssetId))));

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

        @Test
        public void testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityExpectSuccess() throws Exception {
            // the keyPrefix ensures that this test can run against a persistent database multiple times
            String keyPrefix = UUID.randomUUID().toString();
            // first shell

            AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
            shellPayload.setSpecificAssetIds(null);
            shellPayload.setId(UUID.randomUUID().toString());
            SpecificAssetId asset1 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2","value_2",null);
            SpecificAssetId asset2 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2_1","value_2_1",jwtTokenFactory.tenantTwo().getTenantId());
            SpecificAssetId asset3 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2_2","value_2_2",jwtTokenFactory.tenantThree().getTenantId());

            shellPayload.setSpecificAssetIds(List.of(asset1,asset2,asset3));

            performShellCreateRequest(mapper.writeValueAsString(shellPayload));

            SpecificAssetId sa1 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2","value_2",null);
            SpecificAssetId sa2 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2_1","value_2_1",null);

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds", mapper.writeValueAsString(List.of(sa1,sa2)))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    // ensure that only three results match
                    .andExpect(jsonPath("$", contains(shellPayload.getId())));

            // test with tenantTwo assetId included

            SpecificAssetId specificAssetIdsWithTenantTwoIncluded = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2_2","value_2_2",null);

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds", mapper.writeValueAsString(specificAssetIdsWithTenantTwoIncluded))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            // Test lookup with one assetId for tenant two and one without tenantId

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds", mapper.writeValueAsString(List.of(sa1,sa2)))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    // ensure that only three results match
                    .andExpect(jsonPath("$", contains(shellPayload.getId())));
        }

    }

}
