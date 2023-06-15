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
package org.eclipse.tractusx.semantics.registry.mapper;

import org.eclipse.tractusx.semantics.aas.registry.model.*;
import org.eclipse.tractusx.semantics.registry.model.*;
import org.assertj.core.groups.Tuple;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellDescription;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.model.SubmodelDescription;
import org.eclipse.tractusx.semantics.registry.model.SubmodelEndpoint;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

public class ShellMapperTest {

    private final ShellMapper mapper = new ShellMapperImpl(new SubmodelMapperImpl());

    @Test
    public void testMapFromApiExpectSuccess() {
        AssetAdministrationShellDescriptor aas = createCompleteAasDescriptor();

        Shell shell = mapper.fromApiDto(aas);
        assertThat(shell.getIdExternal()).isEqualTo(aas.getId());
        assertThat(shell.getIdShort()).isEqualTo(aas.getIdShort());

        List<Tuple> expectedIdentifiers = new ArrayList<>(List.of(toIdentifierTuples(aas.getSpecificAssetIds())));

        expectedIdentifiers.add(tuple( ShellIdentifier.GLOBAL_ASSET_ID_KEY, aas.getGlobalAssetId()));


        assertThat(shell.getIdentifiers())
                .extracting("key", "value")
                .containsExactlyInAnyOrder(expectedIdentifiers.toArray(new Tuple[0]));

        assertThat(shell.getDescriptions())
                .extracting("language", "text")
                .contains(toDescriptionTuples(aas.getDescription()));


        assertThat(shell.getSubmodels()).hasSize(1);

        SubmodelDescriptor submodelDescriptor = aas.getSubmodelDescriptors().stream().findFirst().get();
        Endpoint endpoint = submodelDescriptor.getEndpoints().stream().findFirst().get();
        ProtocolInformation protocolInformation = endpoint.getProtocolInformation();

        Submodel submodel = shell.getSubmodels().stream().findFirst().get();
        SubmodelEndpoint submodelEndpoint = submodel.getEndpoints().stream().findFirst().get();


        assertThat(submodel.getIdExternal()).isEqualTo(submodelDescriptor.getId());
        assertThat(submodel.getIdShort()).isEqualTo(submodelDescriptor.getIdShort());

        assertThat(submodelEndpoint.getInterfaceName()).isEqualTo(endpoint.getInterface());


        assertThat(submodelEndpoint.getInterfaceName()).isEqualTo(endpoint.getInterface());
        assertThat(submodelEndpoint.getEndpointProtocol()).isEqualTo(protocolInformation.getEndpointProtocol());
        assertThat(submodelEndpoint.getSubProtocol()).isEqualTo(protocolInformation.getSubprotocol());
        assertThat(submodelEndpoint.getSubProtocolBody()).isEqualTo(protocolInformation.getSubprotocolBody());
        assertThat(submodelEndpoint.getSubProtocolBodyEncoding()).isEqualTo(protocolInformation.getSubprotocolBodyEncoding());
    }

    @Test
    public void testMapToApiExpectSuccess() {
        Shell shell = createCompleteShell();
        AssetAdministrationShellDescriptor aas = mapper.toApiDto(shell);
        assertThat(aas.getId()).isEqualTo(shell.getIdExternal());
        assertThat(aas.getIdShort()).isEqualTo(shell.getIdShort());

        String expectedGlobalAssetId  = shell.getIdentifiers().stream()
                .filter(shellIdentifier -> ShellIdentifier.GLOBAL_ASSET_ID_KEY.equals(shellIdentifier.getKey()))
                .map(ShellIdentifier::getValue).findFirst().get();
        assertThat(aas.getGlobalAssetId()).isEqualTo(expectedGlobalAssetId);


        assertThat(aas.getDescription())
                .extracting("language", "text")
                .contains(createTuplesForShellDescriptionTuples(shell.getDescriptions()));

        assertThat(aas.getSubmodelDescriptors()).hasSize(1);
        SubmodelDescriptor apiSubmodelDescriptor = aas.getSubmodelDescriptors().get(0);

        // submodel mappings
        Submodel submodel = shell.getSubmodels().stream().findFirst().get();
        SubmodelEndpoint submodelEndpoint = submodel.getEndpoints().stream().findFirst().get();
        assertThat(apiSubmodelDescriptor.getId()).isEqualTo(submodel.getIdExternal());
        assertThat(apiSubmodelDescriptor.getIdShort()).isEqualTo(submodel.getIdShort());

        assertThat(apiSubmodelDescriptor.getDescription())
                .extracting("language", "text")
                .contains(createTuplesForSubmodelDescriptionTuples(submodel.getDescriptions()));

        assertThat(apiSubmodelDescriptor.getEndpoints()).hasSize(1);
        Endpoint apiSubmodelEndpoint = apiSubmodelDescriptor.getEndpoints().stream().findFirst().get();

        ProtocolInformation apiProtocolInformation = apiSubmodelEndpoint.getProtocolInformation();
        assertThat(apiSubmodelEndpoint.getInterface()).isEqualTo(submodelEndpoint.getInterfaceName());
        assertThat(apiProtocolInformation.getEndpointProtocol()).isEqualTo(submodelEndpoint.getEndpointProtocol());
        assertThat(apiProtocolInformation.getSubprotocol()).isEqualTo(submodelEndpoint.getSubProtocol());
        assertThat(apiProtocolInformation.getSubprotocolBody()).isEqualTo(submodelEndpoint.getSubProtocolBody());
        assertThat(apiProtocolInformation.getSubprotocolBodyEncoding()).isEqualTo(submodelEndpoint.getSubProtocolBodyEncoding());
    }

    private Shell createCompleteShell() {
        ShellIdentifier shellIdentifier1 = new ShellIdentifier(UUID.randomUUID(), "key1", "value1", null, null);
        ShellIdentifier shellIdentifier2 = new ShellIdentifier(UUID.randomUUID(), "key1", "value1", null, null);
        ShellIdentifier shellIdentifier3 = new ShellIdentifier(UUID.randomUUID(), ShellIdentifier.GLOBAL_ASSET_ID_KEY, "exampleGlobalAssetId", null, null);
        Set<ShellIdentifier> shellIdentifiers = Set.of(shellIdentifier1, shellIdentifier2, shellIdentifier3);

        ShellDescription shellDescription1 = new ShellDescription(UUID.randomUUID(), "en", "example description1");
        ShellDescription shellDescription2 = new ShellDescription(UUID.randomUUID(), "de", "exampleDescription2");

        Set<ShellDescription> shellDescriptions = Set.of(shellDescription1, shellDescription2);


        Submodel submodel = new Submodel(UUID.randomUUID(),
                "submodelIdExternal",
                "submodelIdShort", "submodelSemanticId",
                Set.of(new SubmodelDescription(UUID.randomUUID(), "en", "example submodel description")),
                Set.of(new SubmodelEndpoint(UUID.randomUUID(), "interfaceExample",
                        "endpointAddressExample", "endpointProtocolExample",
                        "endpointProtocolVersionExample", "subProtocolExample"
                        , "subProtocolBodyExample", "subProtocolEncodingExample"
                )),
                null
        );

        return new Shell(UUID.randomUUID(), "idExternalExample", "idShortExample",
                shellIdentifiers, shellDescriptions, Set.of(submodel), null,null);
    }

    private AssetAdministrationShellDescriptor createCompleteAasDescriptor() {
        AssetAdministrationShellDescriptor aas = new AssetAdministrationShellDescriptor();
        aas.setId("identificationExample"  );
        aas.setIdShort("idShortExample");

        String globalAssetID = "globalAssetIdExample";
        aas.setGlobalAssetId( globalAssetID );

        SpecificAssetId specificAssetId1 = new SpecificAssetId();
        specificAssetId1.setName( "identifier1KeyExample" );
        specificAssetId1.setValue( "identifier1ValueExample" );

        SpecificAssetId specificAssetId2 = new SpecificAssetId();
        specificAssetId2.setName( "identifier2KeyExample" );
        specificAssetId2.setValue( "identifier2ValueExample" );

        aas.setSpecificAssetIds(List.of(specificAssetId1, specificAssetId2));

        LangStringTextType description1 = new LangStringTextType();
        description1.setLanguage( "de" );
        description1.setText(  "this is an example description1"  );
        LangStringTextType description2 = new LangStringTextType();
        description2.setLanguage( "en" );
        description2.setText("this is an example description2"  );
        aas.setDescription(List.of(description1, description2));


        ProtocolInformation protocolInformation = new ProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");

        protocolInformation.setHref( "endpointAddressExample");

        protocolInformation.setEndpointProtocolVersion( List.of("endpointProtocolVersionExample","endpointTest") );

        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");
        Endpoint endpoint = new Endpoint();
        endpoint.setInterface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);

        Reference reference = new Reference();
        reference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key key = new Key();
        key.setType( KeyTypes.SUBMODEL );
        key.setValue( "semanticIdExample" );
        reference.setKeys( List.of(key) );

        SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();

        submodelDescriptor.setId( "identificationExample");

        submodelDescriptor.setIdShort("idShortExample");
        submodelDescriptor.setSemanticId(reference);
        submodelDescriptor.setDescription(List.of(description1, description2));
        submodelDescriptor.setEndpoints(List.of(endpoint));
        aas.setSubmodelDescriptors(List.of(submodelDescriptor));
        return aas;
    }

    private Tuple[] createTuplesForShellDescriptionTuples(Set<ShellDescription> descriptions) {
        return descriptions.stream()
                .map(description -> tuple(description.getLanguage(), description.getText()))
                .toArray(Tuple[]::new);
    }

    private Tuple[] createTuplesForSubmodelDescriptionTuples(Set<SubmodelDescription> descriptions) {
        return descriptions.stream()
                .map(description -> tuple(description.getLanguage(), description.getText()))
                .toArray(Tuple[]::new);
    }


    private Tuple[] toIdentifierTuples(List<SpecificAssetId> identifiers) {
        return identifiers.stream()
              .map(identifier -> tuple(identifier.getName(), identifier.getValue()))
              .toArray(Tuple[]::new);
    }

    private Tuple[] toDescriptionTuples(List<LangStringTextType> descriptions) {
        return descriptions.stream()
              .map(description -> tuple(description.getLanguage(), description.getText().toString()))
              .toArray(Tuple[]::new);
    }


}
