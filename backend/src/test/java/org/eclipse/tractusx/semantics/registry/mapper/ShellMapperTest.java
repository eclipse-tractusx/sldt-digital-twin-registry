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
import org.eclipse.tractusx.semantics.aas.registry.model.*;
import org.eclipse.tractusx.semantics.registry.model.*;
import org.junit.jupiter.api.Test;

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
        assertThat( submodel.getSemanticId().getReferredSemanticId().getType().toString()).isEqualTo( "ModelReference" );
        assertThat( submodel.getSemanticId().getKeys() ).hasSize( 1 );
        assertThat(submodel.getSemanticId().getType().toString()).isEqualTo( "ExternalReference" );
        assertThat(submodel.getSubmodelSupplemSemanticIds()).hasSize( 1 );
        assertThat(submodel.getSubmodelSupplemSemanticIds().stream().findFirst().get().getKeys()).hasSize( 1 );

        SubmodelSupplemSemanticIdReferenceKey supplemSemanticIdReferenceKey =
              submodel.getSubmodelSupplemSemanticIds().stream().findFirst().get().getKeys().stream().findFirst().get();
        assertThat( supplemSemanticIdReferenceKey.getValue() )
              .isEqualTo( submodelDescriptor.getSupplementalSemanticId().get( 0 ).getKeys().get( 0 ).getValue() );
        assertThat( shell.getShellKind().getValue() ).isEqualTo( aas.getAssetKind().getValue() );
        assertThat(shell.getShellType()).isEqualTo( aas.getAssetType() );
        assertThat(shell.getDisplayNames().stream().findFirst().get().getLanguage()).isEqualTo( aas.getDisplayName().stream().findFirst().get().getLanguage() );
        assertThat( shell.getShellExtensions() ).hasSize( 1 );
        ShellExtension shellExtension = shell.getShellExtensions().stream().findFirst().get();
        Extension aasExtension = aas.getExtensions().stream().findFirst().get();
        assertThat(shell.getShellExtensions()).hasSize( 1 );
        assertThat( shellExtension.getName() ).isEqualTo(aasExtension.getName()  );
       assertThat( shellExtension.getRefersTo() ).hasSize( 1 );
       assertThat( shellExtension.getSupplementalSemanticIds() ).hasSize( 1 );
       assertThat(shellExtension.getSupplementalSemanticIds().stream().findFirst().get().getType()).isEqualTo( ReferenceType.EXTERNALREFERENCE );
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
        assertThat( apiProtocolInformation.getSecurityAttributes() ).hasSize( 1 );
        assertThat( apiProtocolInformation.getSecurityAttributes().get(0).getKey()).isEqualTo( "submodel security attribute key" );
        assertThat( submodel.getDisplayNames().stream().findFirst().get().getLanguage() )
              .isEqualTo( apiSubmodelDescriptor.getDisplayName().stream().findFirst().get().getLanguage() );
        assertThat( submodel.getDisplayNames().stream().findFirst().get().getText() )
              .isEqualTo( apiSubmodelDescriptor.getDisplayName().stream().findFirst().get().getText() );
        assertThat( apiSubmodelDescriptor.getSemanticId().getReferredSemanticId().getKeys() ).hasSize( 1 );
        assertThat(apiSubmodelDescriptor.getSemanticId().getKeys().get( 0 ).getValue()).isEqualTo( "submodelSemanticIdReferenceKey value" );
        assertThat(apiSubmodelDescriptor.getSemanticId().getType().toString()).isEqualTo( submodel.getSemanticId().getType().toString() );
        assertThat(apiSubmodelDescriptor.getSupplementalSemanticId()).hasSize( 1 );
        assertThat(apiSubmodelDescriptor.getSupplementalSemanticId().get( 0 ).getType().toString())
              .isEqualTo(submodel.getSubmodelSupplemSemanticIds().stream().findFirst().get().getType().toString());
        Extension submodelDescriptorExtension = apiSubmodelDescriptor.getExtensions().get( 0 );
        SubmodelExtension submodelExtension = submodel.getSubmodelExtensions().stream().findFirst().get();
        assertThat( apiSubmodelDescriptor.getExtensions() ).hasSize( 1 );
        assertThat(submodelDescriptorExtension.getName()).isEqualTo(submodelExtension.getName());
        assertThat( submodelDescriptorExtension.getValueType().toString() ).isEqualTo( submodelExtension.getValueType().toString() );
        assertThat( submodelDescriptorExtension.getRefersTo() ).hasSize( 1 );
        assertThat( submodelDescriptorExtension.getSupplementalSemanticIds() ).hasSize( 1 );
        assertThat( aas.getAssetKind().equals( shell.getShellKind() ) );
        assertThat( aas.getAssetType().equals( shell.getShellType() ) );
        assertThat( aas.getDisplayName()).hasSize( 1 );
        assertThat( aas.getDisplayName().stream().findFirst().get().getText() ).isEqualTo( shell.getDisplayNames().stream().findFirst().get().getText() );
        assertThat( aas.getSpecificAssetIds() ).hasSize( 2 );
        assertThat( aas.getSpecificAssetIds().get( 0 ).getSemanticId() ).isNotNull();
        assertThat( aas.getSpecificAssetIds().get( 0 ).getSemanticId().getType().getValue() )
              .isEqualTo( shell.getIdentifiers().stream().findFirst().get().getSemanticId().getType().getValue() );
        assertThat(aas.getSpecificAssetIds().get( 0 ).getExternalSubjectId()  ).isNotNull();
        assertThat(aas.getSpecificAssetIds().get( 0 ).getExternalSubjectId().getKeys().get( 0 ).getValue()  ).isEqualTo( "specificExternalSubjectId" );
        assertThat( aas.getExtensions() ).hasSize( 1 );
        Extension aasExtension = aas.getExtensions().stream().findFirst().get();
        assertThat( aasExtension.getName() ).isEqualTo( shell.getShellExtensions().stream().findFirst().get().getName() );
        assertThat( aasExtension.getRefersTo() ).hasSize( 1 );
        assertThat( aasExtension.getSupplementalSemanticIds() ).hasSize( 1 );
        assertThat( aasExtension.getSupplementalSemanticIds().get( 0 ).getType() ).isEqualTo( ReferenceTypes.EXTERNALREFERENCE );
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

        ShellIdentifierSupplemSemanticReferenceKey shellIdentifierSupplemSemanticReferenceKey =
              new ShellIdentifierSupplemSemanticReferenceKey(UUID.randomUUID(),
                    ReferenceKeyType.ASSETADMINISTRATIONSHELL,
                    "supplemental semantic reference key",null,null);
        ShellIdentifierSupplemSemanticReferenceParent shellIdentifierSupplemSemanticReferenceParent =
              new ShellIdentifierSupplemSemanticReferenceParent(
                    UUID.randomUUID(),
                    ReferenceType.MODELREFERENCE,
                    Set.of(shellIdentifierSupplemSemanticReferenceKey),null );
        ShellIdentifierSupplemSemanticReference shellIdentifierSupplemSemanticReference = new ShellIdentifierSupplemSemanticReference(
              UUID.randomUUID(),
              ReferenceType.EXTERNALREFERENCE,
              Set.of(shellIdentifierSupplemSemanticReferenceKey),
              shellIdentifierSupplemSemanticReferenceParent,null );
        ShellIdentifier shellIdentifier1 = new ShellIdentifier( UUID.randomUUID(), "key1", "value1", externalSubjectReference, null, identifierSemanticReference , Set.of(shellIdentifierSupplemSemanticReference));
        ShellIdentifier shellIdentifier2 = new ShellIdentifier( UUID.randomUUID(), "key1", "value2", externalSubjectReference,
              null, identifierSemanticReference , Set.of(shellIdentifierSupplemSemanticReference));

        ShellIdentifier shellIdentifier3 = new ShellIdentifier( UUID.randomUUID(), ShellIdentifier.GLOBAL_ASSET_ID_KEY, "exampleGlobalAssetId", null, null, identifierSemanticReference, Set.of(shellIdentifierSupplemSemanticReference));

        Set<ShellIdentifier> shellIdentifiers = Set.of(shellIdentifier1, shellIdentifier2, shellIdentifier3);

        ShellDescription shellDescription1 = new ShellDescription(UUID.randomUUID(), "en", "example description1",null);
        ShellDescription shellDescription2 = new ShellDescription(UUID.randomUUID(), "de", "exampleDescription2",null);

        Set<ShellDescription> shellDescriptions = Set.of(shellDescription1, shellDescription2);

        SubmodelExtensionRefersToReferenceKey submodelExtensionRefersToReferenceKey = new SubmodelExtensionRefersToReferenceKey(
              UUID.randomUUID(),
              ReferenceKeyType.ANNOTATEDRELATIONSHIPELEMENT,
              "submodelExtensionRefersToReferenceKey value",null,null );

        SubmodelExtensionRefersToReferenceParent submodelExtensionRefersToReferenceParent = new SubmodelExtensionRefersToReferenceParent(
              UUID.randomUUID(),
              ReferenceType.EXTERNALREFERENCE,
              Set.of(submodelExtensionRefersToReferenceKey),null );

        SubmodelExtensionRefersToReference submodelExtensionRefersToReference = new SubmodelExtensionRefersToReference(
              UUID.randomUUID(),ReferenceType.EXTERNALREFERENCE,
              Set.of(submodelExtensionRefersToReferenceKey),
              submodelExtensionRefersToReferenceParent,null );

        SubmodelExtensionSupplemSemanticIdReferenceKey submodelExtensionSupplemSemanticIdReferenceKey = new SubmodelExtensionSupplemSemanticIdReferenceKey(
              UUID.randomUUID(),
              ReferenceKeyType.ANNOTATEDRELATIONSHIPELEMENT,
              "SubmodelExtensionSupplemSemanticIdReferenceKey value",null,null );

        SubmodelExtensionSupplemSemanticIdReferenceParent submodelExtensionSupplemSemanticIdReferenceParent = new SubmodelExtensionSupplemSemanticIdReferenceParent(
              UUID.randomUUID(),
              ReferenceType.EXTERNALREFERENCE,
              Set.of(submodelExtensionSupplemSemanticIdReferenceKey),null );

        SubmodelExtensionSupplemSemanticIdReference submodelExtensionSupplemSemanticIdReference = new SubmodelExtensionSupplemSemanticIdReference(
              UUID.randomUUID(),ReferenceType.EXTERNALREFERENCE,
              Set.of(submodelExtensionSupplemSemanticIdReferenceKey),
              submodelExtensionSupplemSemanticIdReferenceParent,null );

        SubmodelExtensionSemanticIdReferenceKey submodelExtensionSemanticIdReferenceKey = new SubmodelExtensionSemanticIdReferenceKey(
              UUID.randomUUID(),
              ReferenceKeyType.ANNOTATEDRELATIONSHIPELEMENT,
              "SubmodelExtensionSemanticIdReferenceKey value",null,null );

        SubmodelExtensionSemanticIdReferenceParent submodelExtensionSemanticIdReferenceParent = new SubmodelExtensionSemanticIdReferenceParent(
              UUID.randomUUID(),
              ReferenceType.EXTERNALREFERENCE,
              Set.of(submodelExtensionSemanticIdReferenceKey),null );

        SubmodelExtensionSemanticIdReference submodelExtensionSemanticIdReference = new SubmodelExtensionSemanticIdReference(
              UUID.randomUUID(),ReferenceType.EXTERNALREFERENCE,
              Set.of(submodelExtensionSemanticIdReferenceKey),
              submodelExtensionSemanticIdReferenceParent,null );
        SubmodelExtension submodelExtension = new SubmodelExtension( UUID.randomUUID(),submodelExtensionSemanticIdReference, Set.of(submodelExtensionSupplemSemanticIdReference),
              "SubmodelExtension", DataTypeXsd.STRING,"SubmodelExtension value", Set.of(submodelExtensionRefersToReference),null  );

        SubmodelDisplayName submodelDisplayName = new SubmodelDisplayName( UUID.randomUUID(), "de", "Submodel display name",null );
        SubmodelSecurityAttribute submodelSecurityAttribute = new SubmodelSecurityAttribute( UUID.randomUUID(), SubmodelSecurityType.W3C_DID, "submodel security attribute key", "submodel security attribute value",null );

        SubmodelSemanticIdReferenceKey submodelSemanticIdReferenceKey =
              new SubmodelSemanticIdReferenceKey(UUID.randomUUID(),ReferenceKeyType.SUBMODEL,"submodelSemanticIdReferenceKey value",null,null );

        SubmodelSemanticIdReferenceParent submodelSemanticIdReferenceParent =
              new SubmodelSemanticIdReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(submodelSemanticIdReferenceKey),null);
        SubmodelSemanticIdReference submodelSemanticIdReference = new SubmodelSemanticIdReference( UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(submodelSemanticIdReferenceKey), submodelSemanticIdReferenceParent,null);

        SubmodelSupplemSemanticIdReferenceKey submodelSupplemSemanticIdReferenceKey =
              new SubmodelSupplemSemanticIdReferenceKey(UUID.randomUUID(),ReferenceKeyType.SUBMODEL,"SubmodelSupplemSemanticIdReferenceKey value",null,null );

        SubmodelSupplemSemanticIdReferenceParent submodelSupplemSemanticIdReferenceParent =
              new SubmodelSupplemSemanticIdReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(submodelSupplemSemanticIdReferenceKey),null);

        SubmodelSupplemSemanticIdReference submodelSupplemSemanticIdReference = new SubmodelSupplemSemanticIdReference( UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(submodelSupplemSemanticIdReferenceKey),
              submodelSupplemSemanticIdReferenceParent,null);
                Submodel submodel = new Submodel(UUID.randomUUID(), "submodelIdExternal", "submodelIdShort", submodelSemanticIdReference,
                Set.of(new SubmodelDescription(UUID.randomUUID(), "en", "example submodel description",null)),
                Set.of(new SubmodelEndpoint(UUID.randomUUID(), "interfaceExample",
                        "endpointAddressExample", "endpointProtocolExample",
                        "endpointProtocolVersionExample", "subProtocolExample"
                        , "subProtocolBodyExample", "subProtocolEncodingExample",
                      Set.of(submodelSecurityAttribute),null
                )), null, Set.of(submodelDisplayName), Set.of(submodelExtension), Set.of(submodelSupplemSemanticIdReference) );

        ShellDisplayName shellDisplayName = new ShellDisplayName( UUID.randomUUID(), "de", "Display name",null );

        ShellExtensionRefersToReferenceKey refersToReferenceKey =
              new ShellExtensionRefersToReferenceKey(UUID.randomUUID(), ReferenceKeyType.BLOB, "refersToReferenceKey value",null,null);
        ShellExtensionRefersToReferenceParent refersToReferenceParent =
              new ShellExtensionRefersToReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(refersToReferenceKey),null);
        ShellExtensionRefersToReference refersToReference =
              new ShellExtensionRefersToReference(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(refersToReferenceKey), refersToReferenceParent,null);
        ShellExtensionSupplemSemanticIdReferenceKey supplemSemanticIdReferenceKey =
              new ShellExtensionSupplemSemanticIdReferenceKey(UUID.randomUUID(), ReferenceKeyType.BLOB, "supplem SemanticIdReferenceKey value",null,null);
        ShellExtensionSupplemSemanticIdReferenceParent supplemSemanticIdReferenceParent =
              new ShellExtensionSupplemSemanticIdReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(supplemSemanticIdReferenceKey),null);
        ShellExtensionSupplemSemanticIdReference supplemSemanticIdReference =
              new ShellExtensionSupplemSemanticIdReference(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(supplemSemanticIdReferenceKey), supplemSemanticIdReferenceParent,null);
        ShellExtensionSemanticIdReferenceKey semanticIdReferenceKey =
              new ShellExtensionSemanticIdReferenceKey(UUID.randomUUID(), ReferenceKeyType.BLOB, "SemanticIdReferenceKey value",null,null);
        ShellExtensionSemanticIdReferenceParent semanticIdReferenceParent =
              new ShellExtensionSemanticIdReferenceParent(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(semanticIdReferenceKey),null);
        ShellExtensionSemanticIdReference semanticIdReference =
              new ShellExtensionSemanticIdReference(UUID.randomUUID(), ReferenceType.EXTERNALREFERENCE, Set.of(semanticIdReferenceKey), semanticIdReferenceParent,null);

        ShellExtension shellExtension = new ShellExtension( UUID.randomUUID(), "shell extension", "shell extension value", DataTypeXsd.BOOLEAN, semanticIdReference, Set.of(supplemSemanticIdReference), Set.of(refersToReference),null );
        return new Shell(UUID.randomUUID(), "idExternalExample", "idShortExample",
              shellIdentifiers, shellDescriptions, Set.of(submodel),Set.of(shellDisplayName), null,null, ShellKind.INSTANCE, "shellType", Set.of(shellExtension));
    }

    private AssetAdministrationShellDescriptor createCompleteAasDescriptor() {
        AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = new AssetAdministrationShellDescriptor();
        assetAdministrationShellDescriptor.setId("identificationExample"  );
        assetAdministrationShellDescriptor.setIdShort("idShortExample");
        String globalAssetID = "globalAssetIdExample";
        assetAdministrationShellDescriptor.setGlobalAssetId( globalAssetID );
        assetAdministrationShellDescriptor.setAssetType( "AssetType" );
        assetAdministrationShellDescriptor.setAssetKind( AssetKind.INSTANCE );

        LangStringNameType aasDisplayName = new LangStringNameType();
        aasDisplayName.setLanguage( "en" );
        aasDisplayName.setText( "AAS Display Name" );
        assetAdministrationShellDescriptor.setDisplayName(List.of( aasDisplayName) );

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
        assetAdministrationShellDescriptor.setSpecificAssetIds(List.of(specificAssetId1, specificAssetId2));

        LangStringTextType description1 = new LangStringTextType();
        description1.setLanguage( "de" );
        description1.setText(  "this is an example description1"  );
        LangStringTextType description2 = new LangStringTextType();
        description2.setLanguage( "en" );
        description2.setText("this is an example description2"  );
        assetAdministrationShellDescriptor.setDescription(List.of(description1, description2));

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
        assetAdministrationShellDescriptor.setExtensions( List.of(aasExtension) );

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

        Reference submodelSemanticReference = new Reference();
        submodelSemanticReference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key key = new Key();
        key.setType( KeyTypes.SUBMODEL );
        key.setValue( "semanticId Example" );
        ReferenceParent semanticReferenceParent = new ReferenceParent();
        semanticReferenceParent.setKeys( List.of(key) );
        semanticReferenceParent.setType( ReferenceTypes.MODELREFERENCE );

        submodelSemanticReference.setKeys( List.of(key) );
        submodelSemanticReference.setReferredSemanticId( semanticReferenceParent );

        Reference submodelSupplemSemanticIdReference = new Reference();
        submodelSupplemSemanticIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key submodelSupplemSemanticIdkey = new Key();
        submodelSupplemSemanticIdkey.setType( KeyTypes.SUBMODEL );
        submodelSupplemSemanticIdkey.setValue( "supplementalsemanticIdExample value" );

        ReferenceParent submodelSupplemSemanticIdReferenceParent = new ReferenceParent();
        submodelSupplemSemanticIdReferenceParent.setKeys( List.of(submodelSupplemSemanticIdkey) );
        submodelSupplemSemanticIdReferenceParent.setType( ReferenceTypes.MODELREFERENCE );

        submodelSupplemSemanticIdReference.setKeys( List.of(submodelSupplemSemanticIdkey) );
        submodelSupplemSemanticIdReference.setReferredSemanticId( submodelSupplemSemanticIdReferenceParent );

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
       submodelDescriptor.setSupplementalSemanticId( List.of(submodelSupplemSemanticIdReference) );
        assetAdministrationShellDescriptor.setSubmodelDescriptors(List.of(submodelDescriptor));
        return assetAdministrationShellDescriptor;
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
