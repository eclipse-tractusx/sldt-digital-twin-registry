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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.groups.Tuple;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetKind;
import org.eclipse.tractusx.semantics.aas.registry.model.DataTypeDefXsd;
import org.eclipse.tractusx.semantics.aas.registry.model.Endpoint;
import org.eclipse.tractusx.semantics.aas.registry.model.Extension;
import org.eclipse.tractusx.semantics.aas.registry.model.Key;
import org.eclipse.tractusx.semantics.aas.registry.model.KeyTypes;
import org.eclipse.tractusx.semantics.aas.registry.model.LangStringNameType;
import org.eclipse.tractusx.semantics.aas.registry.model.LangStringTextType;
import org.eclipse.tractusx.semantics.aas.registry.model.ProtocolInformation;
import org.eclipse.tractusx.semantics.aas.registry.model.ProtocolInformationSecurityAttributes;
import org.eclipse.tractusx.semantics.aas.registry.model.Reference;
import org.eclipse.tractusx.semantics.aas.registry.model.ReferenceTypes;
import org.eclipse.tractusx.semantics.aas.registry.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.aas.registry.model.SubmodelDescriptor;
import org.eclipse.tractusx.semantics.registry.model.DataTypeXsd;
import org.eclipse.tractusx.semantics.registry.model.ReferenceKey;
import org.eclipse.tractusx.semantics.registry.model.ReferenceKeyType;
import org.eclipse.tractusx.semantics.registry.model.ReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.ReferenceType;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellDescription;
import org.eclipse.tractusx.semantics.registry.model.ShellDisplayName;
import org.eclipse.tractusx.semantics.registry.model.ShellExtension;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionRefersToReference;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionRefersToReferenceKey;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionRefersToReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionSemanticIdReference;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionSemanticIdReferenceKey;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionSemanticIdReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionSupplemSemanticIdReference;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionSupplemSemanticIdReferenceKey;
import org.eclipse.tractusx.semantics.registry.model.ShellExtensionSupplemSemanticIdReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReference;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReferenceKey;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSemanticReference;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSemanticReferenceKey;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSemanticReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSupplemSemanticReference;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSupplemSemanticReferenceKey;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierSupplemSemanticReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.ShellKind;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.model.SubmodelDescription;
import org.eclipse.tractusx.semantics.registry.model.SubmodelDisplayName;
import org.eclipse.tractusx.semantics.registry.model.SubmodelEndpoint;
import org.eclipse.tractusx.semantics.registry.model.SubmodelExtension;
import org.eclipse.tractusx.semantics.registry.model.SubmodelExtensionSemanticIdReference;
import org.eclipse.tractusx.semantics.registry.model.SubmodelExtensionSemanticIdReferenceParent;
import org.eclipse.tractusx.semantics.registry.model.SubmodelSecurityAttribute;
import org.eclipse.tractusx.semantics.registry.model.SubmodelSecurityType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        assertThat( submodelEndpoint.getSubmodelSecurityAttribute() ).hasSize( 1 );
        assertThat( submodelEndpoint.getSubmodelSecurityAttribute().stream().findFirst().get().getValue() ).isEqualTo("ProtocolSecurityArrtibute value");

        // new Fields Submodel
        assertThat(submodel.getDisplayNames().stream().findFirst().get().getLanguage())
              .isEqualTo( submodelDescriptor.getDisplayName().stream().findFirst().get().getLanguage() );

        assertThat( submodel.getSubmodelExtensions()).hasSize( 1 );
        SubmodelExtension submodelExtension = submodel.getSubmodelExtensions().stream().findFirst().get();
        assertThat(submodelExtension.getName() ).isEqualTo( submodelDescriptor.getExtensions().get( 0 ).getName() );
        assertThat(submodelExtension.getValue() ).isEqualTo( submodelDescriptor.getExtensions().get( 0 ).getValue() );
        assertThat(submodelExtension.getValueType().toString()).isEqualTo( submodelDescriptor.getExtensions().get(0).getValueType().toString() );
        assertThat( submodelExtension.getSubmodSemanticId().getType().toString() )
              .isEqualTo( submodelDescriptor.getExtensions().get( 0 ).getSemanticId().getType().toString() );
        assertThat( submodelExtension.getRefersTo() ).hasSize( 1 );
        assertThat( submodelExtension.getSubmodSupplementalIds() ).hasSize( 1 );




        // new Fields AAS / Shell
        assertThat( shell.getShellKind().getValue() ).isEqualTo( aas.getAssetKind().getValue() );
        assertThat(shell.getShellType()).isEqualTo( aas.getAssetType() );
        assertThat(shell.getDisplayNames().stream().findFirst().get().getLanguage()).isEqualTo( aas.getDisplayName().stream().findFirst().get().getLanguage() );

        //Extensioins
        assertThat( shell.getShellExtensions() ).hasSize( 1 );
        ShellExtension shellExtension = shell.getShellExtensions().stream().findFirst().get();
        Extension aasExtension = aas.getExtensions().stream().findFirst().get();
        assertThat(shell.getShellExtensions()).hasSize( 1 );
        assertThat( shellExtension.getName() ).isEqualTo(aasExtension.getName()  );
       assertThat( shellExtension.getRefersTo() ).hasSize( 1 );
       assertThat( shellExtension.getSupplementalSemanticIds() ).hasSize( 1 );
       assertThat(shellExtension.getSupplementalSemanticIds().stream().findFirst().get().getType()).isEqualTo( ReferenceType.EXTERNALREFERENCE );

       //specificAssetIds
       assertThat( shell.getIdentifiers() ).hasSize( 3 );
       ShellIdentifier  shellIdentifier = shell.getIdentifiers().stream().filter( shellIdentifier1 -> shellIdentifier1.getSemanticId() != null ).findFirst().get();
       assertThat( shellIdentifier.getSemanticId().getType().getValue() )
             .isEqualTo( aas.getSpecificAssetIds().get( 0 ).getSemanticId().getType().getValue() );
       assertThat( shellIdentifier.getSupplementalSemanticIds() ).hasSize( 1 );
       assertThat( shellIdentifier.getValue() ).isNotNull();
       assertThat(shellIdentifier.getExternalSubjectId().getKeys().stream().findFirst().get().getValue()).isEqualTo( "ExternalSubject key value" );
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

        SubmodelDescription submodelDescription = new SubmodelDescription(UUID.randomUUID(),"en","example submodel description",null);

        assertThat(apiSubmodelDescriptor.getDescription())
                .extracting("language", "text")
                .contains(createTuplesForSubmodelDescriptionTuples(Set.of(submodelDescription)));

        assertThat(apiSubmodelDescriptor.getEndpoints()).hasSize(1);
        Endpoint apiSubmodelEndpoint = apiSubmodelDescriptor.getEndpoints().stream().findFirst().get();

        ProtocolInformation apiProtocolInformation = apiSubmodelEndpoint.getProtocolInformation();
        assertThat(apiSubmodelEndpoint.getInterface()).isEqualTo(submodelEndpoint.getInterfaceName());
        assertThat(apiProtocolInformation.getEndpointProtocol()).isEqualTo(submodelEndpoint.getEndpointProtocol());
        assertThat(apiProtocolInformation.getSubprotocol()).isEqualTo(submodelEndpoint.getSubProtocol());
        assertThat(apiProtocolInformation.getSubprotocolBody()).isEqualTo(submodelEndpoint.getSubProtocolBody());
        assertThat(apiProtocolInformation.getSubprotocolBodyEncoding()).isEqualTo(submodelEndpoint.getSubProtocolBodyEncoding());
        assertThat( apiProtocolInformation.getSecurityAttributes() ).hasSize( 1 );
        assertThat( apiProtocolInformation.getSecurityAttributes().get(0).getKey()).isEqualTo( "submodel security attribute key" );

        assertThat( submodel.getDisplayNames().stream().findFirst().get().getLanguage() )
              .isEqualTo( apiSubmodelDescriptor.getDisplayName().stream().findFirst().get().getLanguage() );
        assertThat( submodel.getDisplayNames().stream().findFirst().get().getText() )
              .isEqualTo( apiSubmodelDescriptor.getDisplayName().stream().findFirst().get().getText() );
        // submodelDescriptorsExtensions
        Extension submodelDescriptorExtension = apiSubmodelDescriptor.getExtensions().get( 0 );
        SubmodelExtension submodelExtension = submodel.getSubmodelExtensions().stream().findFirst().get();
        assertThat( apiSubmodelDescriptor.getExtensions() ).hasSize( 1 );
        assertThat(submodelDescriptorExtension.getName()).isEqualTo(submodelExtension.getName());
        assertThat( submodelDescriptorExtension.getValueType().toString() ).isEqualTo( submodelExtension.getValueType().toString() );
        assertThat( submodelDescriptorExtension.getRefersTo() ).hasSize( 1 );
        assertThat( submodelDescriptorExtension.getSupplementalSemanticIds() ).hasSize( 1 );

        // new AAS fields
        assertThat( aas.getAssetKind().equals( shell.getShellKind() ) );
        assertThat( aas.getAssetType().equals( shell.getShellType() ) );
        assertThat( aas.getDisplayName()).hasSize( 1 );
        assertThat( aas.getDisplayName().stream().findFirst().get().getText() ).isEqualTo( shell.getDisplayNames().stream().findFirst().get().getText() );

        //specificAssetIds
        assertThat( aas.getSpecificAssetIds() ).hasSize( 2 );
        assertThat( aas.getSpecificAssetIds().get( 0 ).getSemanticId() ).isNotNull();
        assertThat( aas.getSpecificAssetIds().get( 0 ).getSemanticId().getType().getValue() )
              .isEqualTo( shell.getIdentifiers().stream().findFirst().get().getSemanticId().getType().getValue() );
        assertThat(aas.getSpecificAssetIds().get( 0 ).getExternalSubjectId()  ).isNotNull();
        assertThat(aas.getSpecificAssetIds().get( 0 ).getExternalSubjectId().getKeys().get( 0 ).getValue()  ).isEqualTo( "specificExternalSubjectId" );

        //Extensions
        assertThat( aas.getExtensions() ).hasSize( 1 );
        Extension aasExtension = aas.getExtensions().stream().findFirst().get();
        assertThat( aasExtension.getName() ).isEqualTo( shell.getShellExtensions().stream().findFirst().get().getName() );
        assertThat( aasExtension.getRefersTo() ).hasSize( 1 );
        assertThat( aasExtension.getSupplementalSemanticIds() ).hasSize( 1 );
        assertThat( aasExtension.getSupplementalSemanticIds().get( 0 ).getType() ).isEqualTo( ReferenceTypes.MODELREFERENCE );
    }

    private Shell createCompleteShell() {

        //ShellIdentifierExternalSubjectReference -> ExternalSubjectID
        ShellIdentifierExternalSubjectReferenceKey externalSubjectReferenceKey =
              new ShellIdentifierExternalSubjectReferenceKey(UUID.randomUUID(),ReferenceKeyType.ASSETADMINISTRATIONSHELL, "specificExternalSubjectId",null,null );

        ShellIdentifierExternalSubjectReferenceParent externalSubjectReferenceParent =
              new ShellIdentifierExternalSubjectReferenceParent( UUID.randomUUID(),ReferenceType.MODELREFERENCE, Set.of(externalSubjectReferenceKey),null  );

        ShellIdentifierExternalSubjectReference externalSubjectReference = new ShellIdentifierExternalSubjectReference(UUID.randomUUID(),
              ReferenceType.MODELREFERENCE,
              Set.of(externalSubjectReferenceKey),
              externalSubjectReferenceParent,null);

        //ShellIdentifierSemanticReference -> semanticID
        ShellIdentifierSemanticReferenceKey identifierSemanticReferenceKey =
              new ShellIdentifierSemanticReferenceKey(UUID.randomUUID(), ReferenceKeyType.SUBMODEL, "semanticReferenceId",null,null);

        ShellIdentifierSemanticReferenceParent identifierSemanticReferenceParent =
              new ShellIdentifierSemanticReferenceParent(UUID.randomUUID(),ReferenceType.MODELREFERENCE, Set.of(identifierSemanticReferenceKey),null);

        ShellIdentifierSemanticReference identifierSemanticReference = new ShellIdentifierSemanticReference(
              UUID.randomUUID(),
              ReferenceType.MODELREFERENCE,
              Set.of(identifierSemanticReferenceKey),
              identifierSemanticReferenceParent,null
        );

        //ShellIdentifierSupplemSemanticReference -> supplementalSemanticIds;
        ShellIdentifierSupplemSemanticReferenceKey shellIdentifierSupplemSemanticReferenceKey =
              new ShellIdentifierSupplemSemanticReferenceKey(UUID.randomUUID(),
                    ReferenceKeyType.ASSETADMINISTRATIONSHELL,
                    "supplemental semantic reference key",null,null);

        ShellIdentifierSupplemSemanticReferenceParent shellIdentifierSupplemSemanticReferenceParent =
              new ShellIdentifierSupplemSemanticReferenceParent(
                    UUID.randomUUID(),
                    ReferenceType.MODELREFERENCE,
                    Set.of(shellIdentifierSupplemSemanticReferenceKey),null
              );

        ShellIdentifierSupplemSemanticReference shellIdentifierSupplemSemanticReference = new ShellIdentifierSupplemSemanticReference(
              UUID.randomUUID(),
              ReferenceType.EXTERNALREFERENCE,
              Set.of(shellIdentifierSupplemSemanticReferenceKey),
              shellIdentifierSupplemSemanticReferenceParent,null
        );


        ShellIdentifier shellIdentifier1 = new ShellIdentifier(
              UUID.randomUUID(),
              "key1",
              "value1",
              externalSubjectReference,
              null,
              identifierSemanticReference ,
              Set.of(shellIdentifierSupplemSemanticReference));

        ShellIdentifier shellIdentifier2 = new ShellIdentifier(
              UUID.randomUUID(),
              "key1",
              "value2",
              externalSubjectReference,
              null,
              identifierSemanticReference ,
              Set.of(shellIdentifierSupplemSemanticReference));

        ShellIdentifier shellIdentifier3 = new ShellIdentifier(
              UUID.randomUUID(),
              ShellIdentifier.GLOBAL_ASSET_ID_KEY,
              "exampleGlobalAssetId",
              null,
              null,
              identifierSemanticReference,
              Set.of(shellIdentifierSupplemSemanticReference));

        Set<ShellIdentifier> shellIdentifiers = Set.of(shellIdentifier1, shellIdentifier2, shellIdentifier3);

        ShellDescription shellDescription1 = new ShellDescription(UUID.randomUUID(), "en", "example description1",null);
        ShellDescription shellDescription2 = new ShellDescription(UUID.randomUUID(), "de", "exampleDescription2",null);

        SubmodelDescription submodelDescription = new SubmodelDescription(UUID.randomUUID(),"en","example submodel description",null);
//        SubmodelEndpoint submodelEndpoint = new SubmodelEndpoint(UUID.randomUUID(), "interfaceExample",
//                                      "endpointAddressExample", "endpointProtocolExample",
//                                      "endpointProtocolVersionExample", "subProtocolExample"
//                                      , "subProtocolBodyExample", "subProtocolEncodingExample",null);
//
//        ShellDescription shellDescription1 = new ShellDescription(UUID.randomUUID(), "en", "example description1");
//        ShellDescription shellDescription2 = new ShellDescription(UUID.randomUUID(), "de", "exampleDescription2");

        Set<ShellDescription> shellDescriptions = Set.of(shellDescription1, shellDescription2);

         //SubmodelExtensionRefersTo
//        SubmodelExtensionRefersToReferenceKey submodelExtensionRefersToReferenceKey = new SubmodelExtensionRefersToReferenceKey(
//              UUID.randomUUID(),
//              ReferenceKeyType.ANNOTATEDRELATIONSHIPELEMENT,
//              "submodelExtensionRefersToReferenceKey value"
//        );
//
//        SubmodelExtensionRefersToReferenceParent submodelExtensionRefersToReferenceParent = new SubmodelExtensionRefersToReferenceParent(
//              UUID.randomUUID(),
//              ReferenceType.EXTERNALREFERENCE,
//              Set.of(submodelExtensionRefersToReferenceKey)
//        );
//
//        SubmodelExtensionRefersToReference submodelExtensionRefersToReference = new SubmodelExtensionRefersToReference(
//              UUID.randomUUID(),ReferenceType.EXTERNALREFERENCE,
//              Set.of(submodelExtensionRefersToReferenceKey),
//              submodelExtensionRefersToReferenceParent
//        );
//
//        //SubmodelExtensionSupplemSemanticId
//        SubmodelExtensionSupplemSemanticIdReferenceKey submodelExtensionSupplemSemanticIdReferenceKey = new SubmodelExtensionSupplemSemanticIdReferenceKey(
//              UUID.randomUUID(),
//              ReferenceKeyType.ANNOTATEDRELATIONSHIPELEMENT,
//              "SubmodelExtensionSupplemSemanticIdReferenceKey value"
//        );
//
//        SubmodelExtensionSupplemSemanticIdReferenceParent submodelExtensionSupplemSemanticIdReferenceParent = new SubmodelExtensionSupplemSemanticIdReferenceParent(
//              UUID.randomUUID(),
//              ReferenceType.EXTERNALREFERENCE,
//              Set.of(submodelExtensionSupplemSemanticIdReferenceKey)
//        );
//
//        SubmodelExtensionSupplemSemanticIdReference submodelExtensionSupplemSemanticIdReference = new SubmodelExtensionSupplemSemanticIdReference(
//              UUID.randomUUID(),ReferenceType.EXTERNALREFERENCE,
//              Set.of(submodelExtensionSupplemSemanticIdReferenceKey),
//              submodelExtensionSupplemSemanticIdReferenceParent
//        );
//
//        //SubmodelExtensionSemanticID
//        SubmodelExtensionSemanticIdReferenceKey submodelExtensionSemanticIdReferenceKey = new SubmodelExtensionSemanticIdReferenceKey(
//              UUID.randomUUID(),
//              ReferenceKeyType.ANNOTATEDRELATIONSHIPELEMENT,
//              "SubmodelExtensionSemanticIdReferenceKey value"
//        );
//
//        SubmodelExtensionSemanticIdReferenceParent submodelExtensionSemanticIdReferenceParent = new SubmodelExtensionSemanticIdReferenceParent(
//              UUID.randomUUID(),
//              ReferenceType.EXTERNALREFERENCE,
//              Set.of(submodelExtensionSemanticIdReferenceKey)
//        );

//        SubmodelExtensionSemanticIdReference submodelExtensionSemanticIdReference = new SubmodelExtensionSemanticIdReference(
//              UUID.randomUUID(),ReferenceType.EXTERNALREFERENCE,
//              Set.of(submodelExtensionSemanticIdReferenceKey),
//              submodelExtensionSemanticIdReferenceParent
//        );


//                SubmodelExtension submodelExtension = new SubmodelExtension( UUID.randomUUID(),submodelExtensionSemanticIdReference, Set.of(submodelExtensionSupplemSemanticIdReference),
//              "SubmodelExtension", DataTypeXsd.STRING,"SubmodelExtension value", Set.of(submodelExtensionRefersToReference)  );
//
//        SubmodelDisplayName submodelDisplayName = new SubmodelDisplayName( UUID.randomUUID(), "de", "Submodel display name" );
//
//        SubmodelSecurityAttribute submodelSecurityAttribute = new SubmodelSecurityAttribute(
//              UUID.randomUUID(),
//              SubmodelSecurityType.W3C_DID,
//              "submodel security attribute key",
//              "submodel security attribute value" );
//
//        Submodel submodel = new Submodel(UUID.randomUUID(),
//                "submodelIdExternal",
//                "submodelIdShort", "submodelSemanticId",
//                Set.of(new SubmodelDescription(UUID.randomUUID(), "en", "example submodel description",null)),
//                Set.of(new SubmodelEndpoint(UUID.randomUUID(), "interfaceExample",
//                        "endpointAddressExample", "endpointProtocolExample",
//                        "endpointProtocolVersionExample", "subProtocolExample"
//                        , "subProtocolBodyExample", "subProtocolEncodingExample",
//                      Set.of(submodelSecurityAttribute),null
//                )),
//                null,
//              Set.of(submodelDisplayName),
//              Set.of(submodelExtension)
//        );

//        ShellDisplayName shellDisplayName = new ShellDisplayName( UUID.randomUUID(), "de", "Display name" );
//
//        //ShellExtensionRefersToReference
//        ShellExtensionRefersToReferenceKey refersToReferenceKey =
//              new ShellExtensionRefersToReferenceKey(UUID.randomUUID(), ReferenceKeyType.BLOB, "refersToReferenceKey value");
//        ShellExtensionRefersToReferenceParent refersToReferenceParent =
//              new ShellExtensionRefersToReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(refersToReferenceKey));
//        ShellExtensionRefersToReference refersToReference =
//              new ShellExtensionRefersToReference(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(refersToReferenceKey), refersToReferenceParent);
//
//
//        //ShellExtensionSupplemSemanticIdReference
//        ShellExtensionSupplemSemanticIdReferenceKey supplemSemanticIdReferenceKey =
//              new ShellExtensionSupplemSemanticIdReferenceKey(UUID.randomUUID(), ReferenceKeyType.BLOB, "supplem SemanticIdReferenceKey value");
//        ShellExtensionSupplemSemanticIdReferenceParent supplemSemanticIdReferenceParent =
//              new ShellExtensionSupplemSemanticIdReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(supplemSemanticIdReferenceKey));
//        ShellExtensionSupplemSemanticIdReference supplemSemanticIdReference =
//              new ShellExtensionSupplemSemanticIdReference(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(supplemSemanticIdReferenceKey), supplemSemanticIdReferenceParent);
//
//        //ShellExtensionSemanticIdReference
//        ShellExtensionSemanticIdReferenceKey semanticIdReferenceKey =
//              new ShellExtensionSemanticIdReferenceKey(UUID.randomUUID(), ReferenceKeyType.BLOB, "SemanticIdReferenceKey value");
//        ShellExtensionSemanticIdReferenceParent semanticIdReferenceParent =
//              new ShellExtensionSemanticIdReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(semanticIdReferenceKey));
//        ShellExtensionSemanticIdReference semanticIdReference =
//              new ShellExtensionSemanticIdReference(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(semanticIdReferenceKey), semanticIdReferenceParent);
//
//        ShellExtension shellExtension = new ShellExtension(
//              UUID.randomUUID(),
//              semanticIdReference,
//              Set.of(supplemSemanticIdReference),
//              "shell extension",
//              DataTypeXsd.BOOLEAN,
//              "shell extension value",
//              Set.of(refersToReference)
//        );

        return null;
//        return new Shell(UUID.randomUUID(), "idExternalExample", "idShortExample",
//              shellIdentifiers, shellDescriptions, Set.of(submodel), null,null, ShellKind.INSTANCE, "shellType", Set.of(shellDisplayName), Set.of(shellExtension));

    }

    private AssetAdministrationShellDescriptor createCompleteAasDescriptor() {
        AssetAdministrationShellDescriptor aas = new AssetAdministrationShellDescriptor();
        aas.setId("identificationExample"  );
        aas.setIdShort("idShortExample");

        String globalAssetID = "globalAssetIdExample";
        aas.setGlobalAssetId( globalAssetID );

        aas.setAssetType( "AssetType" );
        aas.setAssetKind( AssetKind.INSTANCE );

        LangStringNameType aasDisplayName = new LangStringNameType();
        aasDisplayName.setLanguage( "en" );
        aasDisplayName.setText( "AAS Display Name" );
        aas.setDisplayName(List.of( aasDisplayName) );

        org.eclipse.tractusx.semantics.aas.registry.model.ReferenceParent specificAssetIdParent
              = new org.eclipse.tractusx.semantics.aas.registry.model.ReferenceParent();
        specificAssetIdParent.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key specificAssetIdParentKey = new Key();
        specificAssetIdParentKey.setValue( "SpecificAssetId key RefernceParent key" );
        specificAssetIdParentKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
        specificAssetIdParent.setKeys( List.of(specificAssetIdParentKey) );

        Reference specificAssetIdReference = new Reference();
        specificAssetIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key specificAssetIdKey = new Key();
        specificAssetIdKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
        specificAssetIdKey.setValue( "SpecificAssetId key" );
        specificAssetIdReference.setKeys( List.of(specificAssetIdKey) );
        specificAssetIdReference.setReferredSemanticId( specificAssetIdParent );

        Reference externalSubjectIdReference = new Reference();
        externalSubjectIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key subjectIdKey = new Key();
        subjectIdKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
        subjectIdKey.setValue( "ExternalSubject key value" );
        externalSubjectIdReference.setKeys( List.of(subjectIdKey) );
        externalSubjectIdReference.setReferredSemanticId( specificAssetIdParent );


        SpecificAssetId specificAssetId1 = new SpecificAssetId();
        specificAssetId1.setName( "identifier1KeyExample" );
        specificAssetId1.setValue( "identifier1ValueExample" );
        specificAssetId1.setSemanticId(specificAssetIdReference  );
        specificAssetId1.setSupplementalSemanticIds(List.of(specificAssetIdReference)  );
        specificAssetId1.setExternalSubjectId( externalSubjectIdReference );

        SpecificAssetId specificAssetId2 = new SpecificAssetId();
        specificAssetId2.setName( "identifier2KeyExample" );
        specificAssetId2.setValue( "identifier2ValueExample" );
        specificAssetId2.setSemanticId(specificAssetIdReference  );
        specificAssetId2.setSupplementalSemanticIds(List.of(specificAssetIdReference)  );
        specificAssetId2.setExternalSubjectId( externalSubjectIdReference );

        aas.setSpecificAssetIds(List.of(specificAssetId1, specificAssetId2));

        LangStringTextType description1 = new LangStringTextType();
        description1.setLanguage( "de" );
        description1.setText(  "this is an example description1"  );
        LangStringTextType description2 = new LangStringTextType();
        description2.setLanguage( "en" );
        description2.setText("this is an example description2"  );
        aas.setDescription(List.of(description1, description2));

        // shell reference and extension
        org.eclipse.tractusx.semantics.aas.registry.model.ReferenceParent aasReferenceParent
              = new org.eclipse.tractusx.semantics.aas.registry.model.ReferenceParent();

        aasReferenceParent.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key parentKey = new Key();
        parentKey.setValue( "AAS RefernParent key" );
        parentKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
        aasReferenceParent.setKeys( List.of(parentKey) );

        Reference aasReference = new Reference();
        aasReference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key aasKey = new Key();
        aasKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
        aasKey.setValue( "AAS extension key" );
        aasReference.setKeys( List.of(aasKey) );
        aasReference.setReferredSemanticId( aasReferenceParent );


        Extension aasExtension = new Extension();
        aasExtension.setSemanticId( aasReference );
        aasExtension.setSupplementalSemanticIds( List.of(aasReference) );
        aasExtension.setValue( "AAS extension value" );
        aasExtension.setName( "AAS extension name" );
        aasExtension.setValueType( DataTypeDefXsd.ANYURI );
        aasExtension.setRefersTo( List.of(aasReference) );

        aas.setExtensions( List.of(aasExtension) );

        ProtocolInformation protocolInformation = new ProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");

        protocolInformation.setHref( "endpointAddressExample");

        protocolInformation.setEndpointProtocolVersion( List.of("endpointProtocolVersionExample","endpointTest") );

        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");

        ProtocolInformationSecurityAttributes securityAttributes = new ProtocolInformationSecurityAttributes();
        securityAttributes.setKey( "ProtocolSecurityArrtibute key" );
        securityAttributes.setValue( "ProtocolSecurityArrtibute value" );
        securityAttributes.setType( ProtocolInformationSecurityAttributes.TypeEnum.RFC_TLSA );
        protocolInformation.securityAttributes( List.of(securityAttributes) );


        Endpoint endpoint = new Endpoint();
        endpoint.setInterface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);


        // reference of  openapi SemanticId
        Reference submodelSemanticReference = new Reference();
        submodelSemanticReference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key key = new Key();
        key.setType( KeyTypes.SUBMODEL );
        key.setValue( "semanticIdExample" );
        submodelSemanticReference.setKeys( List.of(key) );


        //SubmodelDescriptor Extension:
        Key submodelExtensionKey = new Key();
        submodelExtensionKey.setType( KeyTypes.SUBMODEL );
        submodelExtensionKey.setValue( "submodelExtensionIdExample" );

        org.eclipse.tractusx.semantics.aas.registry.model.ReferenceParent sumodelExtensionParent
              = new org.eclipse.tractusx.semantics.aas.registry.model.ReferenceParent();
        sumodelExtensionParent.setType( ReferenceTypes.MODELREFERENCE );
        sumodelExtensionParent.setKeys( List.of(submodelExtensionKey) );

        Reference submodelExtensionRef = new Reference();
        submodelExtensionRef.setType( ReferenceTypes.MODELREFERENCE );
        submodelExtensionRef.setReferredSemanticId( sumodelExtensionParent );
        submodelExtensionRef.setKeys( List.of(submodelExtensionKey) );

        Extension submodelExtension = new Extension();
        submodelExtension.setRefersTo( List.of(submodelExtensionRef) );
        submodelExtension.setSupplementalSemanticIds( List.of(submodelExtensionRef) );
        submodelExtension.setName( "Submodel Extension name" );
        submodelExtension.setValue( "Submodel Extension value" );
        submodelExtension.setValueType( DataTypeDefXsd.STRING );
        submodelExtension.setSemanticId( submodelExtensionRef );

        SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();

        submodelDescriptor.setId( "identificationExample");

        submodelDescriptor.setIdShort("idShortExample");

        LangStringNameType submodelDisplayName = new LangStringNameType();
        submodelDisplayName.setLanguage( "en" );
        submodelDisplayName.setText( "AAS Display Name" );
        submodelDescriptor.setDisplayName( List.of(submodelDisplayName) );
        submodelDescriptor.setSemanticId(submodelSemanticReference);
        submodelDescriptor.setDescription(List.of(description1, description2));
        submodelDescriptor.setEndpoints(List.of(endpoint));
       submodelDescriptor.setExtensions( List.of(submodelExtension) );
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
