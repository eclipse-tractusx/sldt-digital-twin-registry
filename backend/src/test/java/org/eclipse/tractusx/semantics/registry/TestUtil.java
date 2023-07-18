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

import org.eclipse.tractusx.semantics.aas.registry.model.*;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class TestUtil {

    public static AssetAdministrationShellDescriptor createCompleteAasDescriptor() {
        AssetAdministrationShellDescriptor aas = new AssetAdministrationShellDescriptor();

        LangStringNameType displayName = new LangStringNameType();
        displayName.setLanguage("de");
        displayName.setText("this is an example description1");
        aas.setDisplayName(List.of(displayName));

        aas.setId("fb7ebcc2-5731-4948-aeaa-c9e9692decf5");

        aas.setIdShort("idShortExample");

        SpecificAssetId specificAssetId1 = new SpecificAssetId();
        specificAssetId1.setName("identifier1KeyExample");
        specificAssetId1.setValue("identifier1ValueExample");

        SpecificAssetId specificAssetId2 = new SpecificAssetId();
        specificAssetId2.setName("identifier2KeyExample");
        specificAssetId2.setValue("identifier2ValueExample");

        aas.setSpecificAssetIds(List.of(specificAssetId1, specificAssetId2));

        LangStringTextType description1 = new LangStringTextType();
        description1.setLanguage("de");
        description1.setText("hello text");
        LangStringTextType description2 = new LangStringTextType();
        description2.setLanguage("en");
        description2.setText("hello s");
        aas.setDescription(List.of(description1, description2));

        aas.setDescription(List.of(description1, description2));

        ProtocolInformation protocolInformation = new ProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");

        protocolInformation.setHref("endpointAddressExample");

        protocolInformation.setEndpointProtocolVersion(List.of("e"));

        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");

        ProtocolInformationSecurityAttributes securityAttributes = new ProtocolInformationSecurityAttributes();
        securityAttributes.setType(ProtocolInformationSecurityAttributes.TypeEnum.NONE);
        protocolInformation.setSecurityAttributes(List.of(securityAttributes));

        Endpoint endpoint = new Endpoint();
        endpoint.setInterface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);

        Reference reference = new Reference();
        reference.setType(ReferenceTypes.EXTERNALREFERENCE);
        Key key = new Key();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue("semanticIdExample");
        reference.setKeys(List.of(key));

        Extension extension = new Extension();
        extension.setRefersTo(List.of(reference));
        extension.addSupplementalSemanticIdsItem(reference);

        aas.setExtensions(List.of(extension));

        SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();

        submodelDescriptor.setId(UUID.randomUUID().toString());

        submodelDescriptor.setIdShort("idShortExample");
        submodelDescriptor.setSemanticId(reference);
        specificAssetId1.setSupplementalSemanticIds(List.of(reference));
        specificAssetId2.setSupplementalSemanticIds(List.of(reference));

        submodelDescriptor.setDescription(List.of(description1, description2));
        submodelDescriptor.setEndpoints(List.of(endpoint));
        aas.setEndpoints(List.of(endpoint));
        aas.setSubmodelDescriptors(List.of(submodelDescriptor));

        return aas;
    }

    public static SubmodelDescriptor createSubmodel(){
        SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();
        submodelDescriptor.setId(UUID.randomUUID().toString());
        Reference reference = new Reference();
        reference.setType(ReferenceTypes.EXTERNALREFERENCE);
        Key key = new Key();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue("semanticIdExample");
        reference.setKeys(List.of(key));
        submodelDescriptor.setIdShort("idShortExample");
        submodelDescriptor.setSemanticId(reference);
        LangStringTextType description1 = new LangStringTextType();
        description1.setLanguage("de");
        description1.setText("hello text");
        LangStringTextType description2 = new LangStringTextType();
        description2.setLanguage("en");
        description2.setText("hello s");

        ProtocolInformation protocolInformation = new ProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");

        protocolInformation.setHref("endpointAddressExample");

        protocolInformation.setEndpointProtocolVersion(List.of("e"));

        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");

        Endpoint endpoint = new Endpoint();
        endpoint.setInterface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);

        submodelDescriptor.setDescription(List.of(description1, description2));
        submodelDescriptor.setEndpoints(List.of(endpoint));

        return submodelDescriptor;
    }

    public static SpecificAssetId createSpecificAssetId(){
        SpecificAssetId specificAssetId1 = new SpecificAssetId();
        specificAssetId1.setName("identifier1KeyExample");
        specificAssetId1.setValue("identifier1ValueExample");

        Reference reference = new Reference();
        reference.setType(ReferenceTypes.EXTERNALREFERENCE);
        Key key = new Key();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue("key");
        reference.setKeys(List.of(key));

        specificAssetId1.setSupplementalSemanticIds(List.of(reference));

        return specificAssetId1;
    }

    public static SpecificAssetId createSpecificAssetId(String name, String value, String externalSubjectId){
        SpecificAssetId specificAssetId1 = new SpecificAssetId();
        specificAssetId1.setName(name);
        specificAssetId1.setValue(value);

        if(externalSubjectId!=null){
            Reference reference = new Reference();
            reference.setType(ReferenceTypes.EXTERNALREFERENCE);
            Key key = new Key();
            key.setType(KeyTypes.SUBMODEL);
            key.setValue(externalSubjectId);
            reference.setKeys(List.of(key));

            specificAssetId1.setExternalSubjectId(reference);
        }
        return specificAssetId1;
    }

    public static String getEncodedValue(String shellId){
       return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
    }
}