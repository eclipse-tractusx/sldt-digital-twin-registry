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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.registry.repository.ShellRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties( RegistryProperties.class)
public abstract class AbstractAssetAdministrationShellApi {

    protected static final String SHELL_BASE_PATH = "/api/v3.0/shell-descriptors";
    protected static final String SINGLE_SHELL_BASE_PATH = "/api/v3.0/shell-descriptors/{aasIdentifier}";
    protected static final String LOOKUP_SHELL_BASE_PATH = "/api/v3.0/lookup/shells";
    protected static final String SINGLE_LOOKUP_SHELL_BASE_PATH = "/api/v3.0/lookup/shells/{aasIdentifier}";
    protected static final String SUB_MODEL_BASE_PATH = "/api/v3.0/shell-descriptors/{aasIdentifier}/submodel-descriptors";
    protected static final String SINGLE_SUB_MODEL_BASE_PATH = "/api/v3.0/shell-descriptors/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}";

    protected static final String EXTERNAL_SUBJECT_ID_HEADER = "Edc-Bpn";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected JwtTokenFactory jwtTokenFactory;

    @Autowired
    private RegistryProperties registryProperties;

    @Autowired
    protected ShellRepository shellRepository;

    protected String getId(ObjectNode payload) {
        return payload.get("identification").textValue();
    }

    protected String getExternalSubjectIdWildcardPrefix(){
        return registryProperties.getExternalSubjectIdWildcardPrefix();
    }

    protected void performSubmodelCreateRequest(String payload, String shellIdentifier) throws Exception {
        mvc.perform(
                        MockMvcRequestBuilders
                                .post(SUB_MODEL_BASE_PATH, shellIdentifier)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .with(jwtTokenFactory.allRoles())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(content().json(payload));
    }

    /**
     * calls create and checks result for identity
     * @param payload
     * @throws Exception
     */
    protected void performShellCreateRequest(String payload) throws Exception {
        performShellCreateRequest(payload,payload);
    }

    /**
     * performs create and checks result for expections
     * @param payload
     * @param expectation
     * @throws Exception
     */
    protected void performShellCreateRequest(String payload, String expectation) throws Exception {
        mvc.perform(
                        MockMvcRequestBuilders
                                .post(SHELL_BASE_PATH)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .with(jwtTokenFactory.allRoles())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(content().json(expectation));
    }


    protected ObjectNode createShell() throws JsonProcessingException {
        ObjectNode shellPayload = createBaseIdPayload("exampleShellIdPrefix", "exampleShellShortId");
        shellPayload.set("description", emptyArrayNode()
                .add(createDescription("en", "this is an example description"))
                .add(createDescription("de", "das ist ein beispiel")));

        String globalId="exampleGlobalAssetId";

        shellPayload.set("globalAssetId", mapper.createObjectNode()
                .set("value", emptyArrayNode().add(globalId) ));

        shellPayload.set("specificAssetIds", emptyArrayNode()
                .add(specificAssetId("vin1", "valueforvin1"))
                .add(specificAssetId("enginenumber1", "enginenumber1")));

        shellPayload.set("submodelDescriptors", emptyArrayNode()
                .add(createSubmodel("submodel_external1"))
                .add(createSubmodel("submodel_external2")));
        return shellPayload;
    }

    protected ObjectNode createSubmodel(String submodelIdPrefix) throws JsonProcessingException {
        ObjectNode submodelPayload = createBaseNewIdPayload(submodelIdPrefix, "exampleSubModelShortId");
        submodelPayload.set("description", emptyArrayNode()
                .add(createDescription("en", "this is an example submodel description"))
                .add(createDescription("de", "das ist ein Beispiel submodel")));
        submodelPayload.set("endpoints", emptyArrayNode()
                .add(createEndpoint()));
        submodelPayload.put("semanticId", createSemanticId());
        return submodelPayload;
    }

    protected static String uuid(String prefix) {
        return prefix + "#" + UUID.randomUUID();
    }


    protected ArrayNode emptyArrayNode() {
        return mapper.createArrayNode();
    }

    protected ObjectNode createBaseIdPayload(String idPrefix, String idShort) throws JsonProcessingException {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("identification", uuid(idPrefix));
        objectNode.put("idShort", idShort);
        return objectNode;
    }

    protected ObjectNode createBaseNewIdPayload(String idPrefix, String idShort) throws JsonProcessingException {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", UUID.randomUUID().toString());
        objectNode.put("idShort", idShort);
        return objectNode;
    }

    protected ObjectNode createDescription(String language, String text) {
        ObjectNode description = mapper.createObjectNode();
        description.put("language", language);
        description.put("text", text);
        return description;
    }

    protected ObjectNode specificAssetId(String key, String value) {
       return specificAssetId(key, value, null);
    }

    protected ObjectNode specificAssetId(String key, String value, String tenantId){

        ObjectNode specificAssetId = mapper.createObjectNode();
        specificAssetId.put("name", key);
        specificAssetId.put("value", value);
        if(tenantId != null){
            specificAssetId.set("externalSubjectId", mapper.createObjectNode()
                    .set("value", emptyArrayNode().add(tenantId) ));
        }
        return specificAssetId;
    }


    protected ObjectNode createSemanticId() {
        ObjectNode semanticId = mapper.createObjectNode();
        semanticId.set("value", emptyArrayNode().add("urn:net.catenax.vehicle:1.0.0#Parts"));
        return semanticId;
    }

    protected ObjectNode createEndpoint() {
        ObjectNode endpoint = mapper.createObjectNode();
        endpoint.put("interface", "interfaceName");
        endpoint.set("protocolInformation", mapper.createObjectNode()
                .put("endpointAddress", "https://catena-xsubmodel-vechile.net/path")
                .put("endpointProtocol", "https")
                .put("subprotocol", "Mca1uf1")
                .put("subprotocolBody", "Mafz1")
                .put("subprotocolBodyEncoding", "Fj1092ufj")
        );
        return endpoint;
    }

    protected String toJson(JsonNode jsonNode) throws JsonProcessingException {
        return mapper.writeValueAsString(jsonNode);
    }

    protected String toJson(ObjectNode objectNode) throws JsonProcessingException {
        return mapper.writeValueAsString(objectNode);
    }

    protected String toJson(ArrayNode objectNode) throws JsonProcessingException {
        return mapper.writeValueAsString(objectNode);
    }

}
