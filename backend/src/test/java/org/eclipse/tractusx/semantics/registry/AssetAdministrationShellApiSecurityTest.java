/********************************************************************************
 * Copyright (c) 2021-2022 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2021-2022 Contributors to the Eclipse Foundation
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

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
            ObjectNode shell = createShell();
            performShellCreateRequest(toJson(shell));
            shellId = getId(shell);
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
            ObjectNode shellPayloadForPost = createShell();
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(shellPayloadForPost))
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SHELL_BASE_PATH, toJson(shellPayloadForPost) )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(shellPayloadForPost))
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated());
        }

        @Test
        public void testRbacForUpdate() throws Exception {
            ObjectNode shellPayloadForUpdate = createShell()
                    .put("identification", shellId);
            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SHELL_BASE_PATH, shellId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(shellPayloadForUpdate))
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());


            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SHELL_BASE_PATH, shellId )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(shellPayloadForUpdate))
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

       @BeforeEach
       public void before() throws Exception{
           ObjectNode shell = createShell();
           performShellCreateRequest(toJson(shell));

           ObjectNode submodel = createSubmodel("submodelIdPrefix");
           performSubmodelCreateRequest(toJson(submodel), getId(shell));

           shellId = getId(shell);
           submodelId = getId(submodel);
       }


       @Test
       public void testRbacForGetAll() throws Exception {
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
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
                                    .accept(MediaType.APPLICATION_JSON)
                                    // test with wrong role
                                    .with(jwtTokenFactory.deleteTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

        @Test
        public void testRbacForCreate() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SUB_MODEL_BASE_PATH, shellId )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(createSubmodel("exampleSubmodel")))
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SUB_MODEL_BASE_PATH, shellId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(createSubmodel("exampleSubmodel")))
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated());

        }

        @Test
        public void testRbacForUpdate() throws Exception {
            ObjectNode submodelToUpdate = createSubmodel("1231")
                    .put("identification", submodelId);
            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(submodelToUpdate))
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .put(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(submodelToUpdate))
                                    .with(jwtTokenFactory.updateTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent());
        }

        @Test
        public void testRbacForDelete() throws Exception {
            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    // test with wrong role
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .delete(SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId)
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
            ObjectNode shell = createShell();
            performShellCreateRequest(toJson(shell));
            shellId = getId(shell);
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
            ArrayNode specificAssetIds = emptyArrayNode().add(specificAssetId("abc", "123"));
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds",  toJson(specificAssetIds))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.addTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden());

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds",  toJson(specificAssetIds))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.readTwin())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

    }

    @Nested
    @DisplayName("Custom AAS API Authorization Tests")
    class CustomAASApiTest {

        @Test
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

        @Test
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

        @Test
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
            ObjectNode shellPayload = createBaseIdPayload("example", "example");

            String tenantTwoAssetIdValue = "tenantTwo23848920932";
            String tenantThreeAssetIdValue = "tenantThree2gdf19023123423";
            String withoutTenantAssetIdValue = "withoutTenant2gdfk1273";
            shellPayload.set("specificAssetIds", emptyArrayNode()
                    .add(specificAssetId("CustomerPartId", tenantTwoAssetIdValue,  jwtTokenFactory.tenantTwo().getTenantId()))
                    .add(specificAssetId("CustomerPartId", tenantThreeAssetIdValue, jwtTokenFactory.tenantThree().getTenantId()))
                    .add(specificAssetId("MaterialNumber",withoutTenantAssetIdValue))
            );
            performShellCreateRequest(toJson(shellPayload));
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SHELL_BASE_PATH)
                                    .queryParam("pageSize", "100")
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").exists())
                    .andExpect(jsonPath("$.items[*].identification", hasItem(getId(shellPayload))))
                    .andExpect(jsonPath("$.items[*].specificAssetIds[*].value", hasItems(tenantThreeAssetIdValue, tenantTwoAssetIdValue, withoutTenantAssetIdValue)));

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
                    .andExpect(jsonPath("$.items").exists())
                    .andExpect(jsonPath("$.items[*].identification", hasItem(getId(shellPayload))))
                    .andExpect(jsonPath("$.items[*].specificAssetIds[*].value", hasItems(tenantTwoAssetIdValue, withoutTenantAssetIdValue)))
                    .andExpect(jsonPath("$.items[*].specificAssetIds[*].value", not(hasItem(tenantThreeAssetIdValue))));
        }

        @Test
        public void testGetShellPayloadWithFilteredSpecificAssetIdsByTenantId() throws Exception {
            ObjectNode shellPayload = createBaseIdPayload("example", "example");
            String tenantTwoAssetIdValue = "tenantTwofgkj12308410239401";
            String tenantThreeAssetIdValue = "tenantThree23408410293o42731";
            String withoutTenantAssetIdValue = "withoutTenant23947192jf18";
            shellPayload.set("specificAssetIds", emptyArrayNode()
                    .add(specificAssetId("CustomerPartId", tenantTwoAssetIdValue,  jwtTokenFactory.tenantTwo().getTenantId()))
                    .add(specificAssetId("CustomerPartId", tenantThreeAssetIdValue, jwtTokenFactory.tenantThree().getTenantId()))
                    .add(specificAssetId("MaterialNumber",withoutTenantAssetIdValue))
            );
            performShellCreateRequest(toJson(shellPayload));
            String shellId = getId(shellPayload);
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.identification", equalTo(shellId)))
                    .andExpect(jsonPath("$.specificAssetIds[*].value", containsInAnyOrder(tenantTwoAssetIdValue,tenantThreeAssetIdValue, withoutTenantAssetIdValue)));

            // test with tenant two
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.identification", equalTo(shellId)))
                    .andExpect(jsonPath("$.specificAssetIds[*].value", hasItems(tenantTwoAssetIdValue, withoutTenantAssetIdValue)))
                    .andExpect(jsonPath("$.specificAssetIds[*].value", not(hasItem(tenantThreeAssetIdValue))));
        }

        @Test
        public void testGetSpecificAssetIdsFilteredByTenantId() throws Exception {
            ObjectNode shellPayload = createBaseIdPayload("example", "example");
            performShellCreateRequest(toJson(shellPayload));


            String tenantTwoAssetIdValue = "tenantTwofgkj12308410239401";
            String tenantThreeAssetIdValue = "tenantThree23408410293o42731";
            String withoutTenantAssetIdValue = "withoutTenant23947192jf18";
            ArrayNode specificAssetIds = emptyArrayNode()
                    .add(specificAssetId("CustomerPartId", tenantTwoAssetIdValue,  jwtTokenFactory.tenantTwo().getTenantId()))
                    .add(specificAssetId("CustomerPartId", tenantThreeAssetIdValue, jwtTokenFactory.tenantThree().getTenantId()))
                    .add(specificAssetId("MaterialNumber",withoutTenantAssetIdValue));

            String shellId = getId(shellPayload);
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(specificAssetIds))
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isCreated())
                    .andExpect(content().json(toJson(specificAssetIds)));

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].value", containsInAnyOrder(tenantTwoAssetIdValue,
                            tenantThreeAssetIdValue,
                            withoutTenantAssetIdValue)));

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(SINGLE_LOOKUP_SHELL_BASE_PATH, shellId)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].value", hasItems(tenantTwoAssetIdValue, withoutTenantAssetIdValue)))
                    .andExpect(jsonPath("$[*].value", not(hasItem(tenantThreeAssetIdValue))));
        }

        @Test
        public void testFindExternalShellIdsBySpecificAssetIdsWithTenantBasedVisibilityExpectSuccess() throws Exception {
            // the keyPrefix ensures that this test can run against a persistent database multiple times
            String keyPrefix = UUID.randomUUID().toString();
            // first shell
            ObjectNode firstShellPayload = createBaseIdPayload("sampleForQuery", "idShortSampleForQuery");
            firstShellPayload.set("specificAssetIds", emptyArrayNode()
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2", "value_2"))
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2_1", "value_2_1",
                            jwtTokenFactory.tenantTwo().getTenantId()))
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2_2", "value_2_2",
                            jwtTokenFactory.tenantThree().getTenantId())));
            performShellCreateRequest(toJson(firstShellPayload));

            ArrayNode specificAssetIds = emptyArrayNode()
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2", "value_2"))
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2_1", "value_2_1"));

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds", toJson(specificAssetIds))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    // ensure that only three results match
                    .andExpect(jsonPath("$", contains(getId(firstShellPayload))));

            // test with tenantTwo assetId included
            ArrayNode specificAssetIdsWithTenantTwoIncluded = specificAssetIds
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2_2", "value_2_2"));
            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds", toJson(specificAssetIdsWithTenantTwoIncluded))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            // Test lookup with one assetId for tenant two and one without tenantId
            ArrayNode specificAssetIdsTenantTwo = emptyArrayNode()
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2", "value_2"))
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2_1", "value_2_1"));

            mvc.perform(
                            MockMvcRequestBuilders
                                    .get(LOOKUP_SHELL_BASE_PATH)
                                    .queryParam("assetIds", toJson(specificAssetIdsTenantTwo))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    // ensure that only three results match
                    .andExpect(jsonPath("$", contains(getId(firstShellPayload))));
        }

        @Test
        public void testFindExternalShellIdsBySpecificAssetIdsWithAnyMatchExpectSuccess() throws Exception {
            // the keyPrefix ensures that this test can run against a persistent database multiple times
            String keyPrefix = UUID.randomUUID().toString();
            ObjectNode commonAssetId = specificAssetId(keyPrefix + "commonAssetIdKey", "commonAssetIdValue");
            // first shell
            ObjectNode firstShellPayload = createBaseIdPayload("sampleForQuery", "idShortSampleForQuery");
            firstShellPayload.set("specificAssetIds", emptyArrayNode()
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_1", "value_1")));
            performShellCreateRequest(toJson(firstShellPayload));

            // second shell
            ObjectNode secondShellPayload = createBaseIdPayload("sampleForQuery", "idShortSampleForQuery");
            secondShellPayload.set("specificAssetIds", emptyArrayNode()
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2", "value_2", jwtTokenFactory.tenantTwo().getTenantId())));
            performShellCreateRequest(toJson(secondShellPayload));

            // third shell
            ObjectNode thirdShellPayload = createBaseIdPayload("sampleForQuery", "idShortSampleForQuery");
            thirdShellPayload.set("specificAssetIds", emptyArrayNode()
                    .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_3", "value_3", jwtTokenFactory.tenantThree().getTenantId())));
            performShellCreateRequest(toJson(thirdShellPayload));

            // query to retrieve any match
            JsonNode anyMatchAueryByAssetIds = mapper.createObjectNode().set("query", mapper.createObjectNode()
                    .set("assetIds",  emptyArrayNode()
                            .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_1", "value_1"))
                            .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_2", "value_2"))
                            .add(specificAssetId(keyPrefix + "findExternalShellIdQueryKey_3", "value_3"))
                            .add(commonAssetId))
            );

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(LOOKUP_SHELL_BASE_PATH + "/query")
                                    .content(toJson(anyMatchAueryByAssetIds))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$", containsInAnyOrder(getId(firstShellPayload), getId(secondShellPayload), getId(thirdShellPayload))));

            mvc.perform(
                            MockMvcRequestBuilders
                                    .post(LOOKUP_SHELL_BASE_PATH + "/query")
                                    .content(toJson(anyMatchAueryByAssetIds))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(jwtTokenFactory.tenantTwo().allRoles())
                    )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$", containsInAnyOrder(getId(firstShellPayload), getId(secondShellPayload))));
        }

    }

}
