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

        aas.setGlobalAssetId( "globalAssetId example" );

        aas.setAssetType( "AssetType" );
        aas.setAssetKind( AssetKind.INSTANCE );

        aas.setId("fb7ebcc2-5731-4948-aeaa-c9e9692decf5");

        aas.setIdShort("idShortExample");


       ReferenceParent specificAssetIdRefParent = new ReferenceParent();
       specificAssetIdRefParent.setType( ReferenceTypes.EXTERNALREFERENCE );
       Key specificAssetIdParentKey = new Key();
       specificAssetIdParentKey.setValue( "specificAssetId ReferenceParent key" );
       specificAssetIdParentKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
       specificAssetIdRefParent.setKeys( List.of(specificAssetIdParentKey) );

       Reference specificAssetIdReference = new Reference();
       specificAssetIdReference.setType( ReferenceTypes.MODELREFERENCE );
       Key specificAssetIdKey = new Key();
       specificAssetIdKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
       specificAssetIdKey.setValue( "specificAssetIdReference key" );
       specificAssetIdReference.setKeys( List.of(specificAssetIdKey) );
       specificAssetIdReference.setReferredSemanticId( specificAssetIdRefParent );

       Reference externalSubjectIdReference = new Reference();
       externalSubjectIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
       Key subjectIdKey = new Key();
       subjectIdKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
       subjectIdKey.setValue( "ExternalSubject key value" );
       externalSubjectIdReference.setKeys( List.of(subjectIdKey) );
       externalSubjectIdReference.setReferredSemanticId( specificAssetIdRefParent );


       Key assetIdKey = new Key();
       assetIdKey.setType( KeyTypes.BASICEVENTELEMENT );
       assetIdKey.setValue( "assetIdKey value" );

       ReferenceParent assetIdParent = new ReferenceParent();
       assetIdParent.setType( ReferenceTypes.EXTERNALREFERENCE );
       assetIdParent.setKeys( List.of(assetIdKey) );

       Reference assetIdReference = new Reference();
       assetIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
       assetIdReference.setKeys( List.of(assetIdKey) );
       assetIdReference.setReferredSemanticId( assetIdParent );

        SpecificAssetId specificAssetId1 = new SpecificAssetId();
        specificAssetId1.setName("identifier1KeyExample");
        specificAssetId1.setValue("identifier1ValueExample");
        specificAssetId1.setSemanticId( specificAssetIdReference );
        specificAssetId1.setSupplementalSemanticIds( List.of(assetIdReference) );
        specificAssetId1.setExternalSubjectId(externalSubjectIdReference  );

       SpecificAssetId specificAssetId2 = new SpecificAssetId();
       specificAssetId2.setName("identifier2KeyExample");
       specificAssetId2.setValue("identifier2ValueExample");
       specificAssetId2.setSemanticId( specificAssetIdReference );
       specificAssetId2.setSupplementalSemanticIds( List.of(assetIdReference) );
       specificAssetId2.setExternalSubjectId( externalSubjectIdReference );


       aas.setSpecificAssetIds(List.of(specificAssetId1, specificAssetId2));

        LangStringTextType description1 = new LangStringTextType();
        description1.setLanguage("de");
        description1.setText("hello text");
        LangStringTextType description2 = new LangStringTextType();
        description2.setLanguage("en");
        description2.setText("hello s");
        aas.setDescription(List.of(description1, description2));

        aas.setDescription(List.of(description1, description2));

       ReferenceParent aasReferenceParent = new ReferenceParent();

        aasReferenceParent.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key parentKey = new Key();
        parentKey.setValue( "AAS RefernParent key" );
        parentKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
        aasReferenceParent.setKeys( List.of(parentKey) );

        Reference aasReference = new Reference();
        aasReference.setType( ReferenceTypes.MODELREFERENCE );
        Key aasKey = new Key();
        aasKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
        aasKey.setValue( "AAS extension key" );
        aasReference.setKeys( List.of(aasKey) );
        aasReference.setReferredSemanticId( aasReferenceParent );

       Reference aasReference2 = new Reference();
       aasReference2.setType( ReferenceTypes.EXTERNALREFERENCE );
       Key aasKey2 = new Key();
       aasKey2.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
       aasKey2.setValue( "AAS extension key" );
       aasReference2.setKeys( List.of(aasKey2) );
       aasReference2.setReferredSemanticId( aasReferenceParent );


       Extension aasExtension = new Extension();
        aasExtension.setSemanticId( aasReference );
        aasExtension.setSupplementalSemanticIds( List.of(aasReference) );
        aasExtension.setValue( "AAS extension value" );
        aasExtension.setName( "AAS extension name" );
        aasExtension.setValueType( DataTypeDefXsd.ANYURI );
        aasExtension.setRefersTo( List.of(aasReference2) );

        aas.setExtensions( List.of(aasExtension) );


        ProtocolInformation protocolInformation = new ProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");

        protocolInformation.setHref("endpointAddressExample");

        protocolInformation.setEndpointProtocolVersion(List.of("e"));

        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");

        ProtocolInformationSecurityAttributes securityAttributes = new ProtocolInformationSecurityAttributes();
        securityAttributes.setType(ProtocolInformationSecurityAttributes.TypeEnum.NONE);
        securityAttributes.setKey( "Security Attribute key" );
        securityAttributes.setValue( "Security Attribute value" );
        protocolInformation.setSecurityAttributes(List.of(securityAttributes));

        Endpoint endpoint = new Endpoint();
        endpoint.setInterface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);

        //semanticID
        Reference submodelSemanticReference = new Reference();
       submodelSemanticReference.setType(ReferenceTypes.EXTERNALREFERENCE);
        Key key = new Key();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue("semanticIdExample");
       submodelSemanticReference.setKeys(List.of(key));

       ReferenceParent semanticReferenceParent = new ReferenceParent();
       semanticReferenceParent.setKeys( List.of(key) );
       semanticReferenceParent.setType( ReferenceTypes.MODELREFERENCE );

       submodelSemanticReference.setKeys( List.of(key) );
       submodelSemanticReference.setReferredSemanticId( semanticReferenceParent );

       //supplemental semanticID
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

       //SubmodelDescriptor Extension:
       Key submodelExtensionKey = new Key();
       submodelExtensionKey.setType( KeyTypes.SUBMODEL );
       submodelExtensionKey.setValue( "submodelExtensionIdExample" );

       ReferenceParent sumodelExtensionParent = new ReferenceParent();
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

        submodelDescriptor.setId(UUID.randomUUID().toString());
        submodelDescriptor.setDisplayName( List.of(displayName) );

        submodelDescriptor.setIdShort("idShortExample");
        submodelDescriptor.setSemanticId(submodelSemanticReference);
        submodelDescriptor.setSupplementalSemanticId( List.of(submodelSupplemSemanticIdReference) );

        submodelDescriptor.setDescription(List.of(description1, description2));
        submodelDescriptor.setEndpoints(List.of(endpoint));
       submodelDescriptor.setExtensions( List.of(submodelExtension) );
        aas.setEndpoints(List.of(endpoint));
        aas.setSubmodelDescriptors(List.of(submodelDescriptor));

        return aas;
    }

    public static SubmodelDescriptor createSubmodel(){
        SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();
        submodelDescriptor.setId(UUID.randomUUID().toString());
       submodelDescriptor.setIdShort("idShortExample");

       Reference submodelSemanticReference = new Reference();
       submodelSemanticReference.setType(ReferenceTypes.EXTERNALREFERENCE);
       Key key = new Key();
       key.setType(KeyTypes.SUBMODEL);
       key.setValue("semanticIdExample");
       submodelSemanticReference.setKeys(List.of(key));

       ReferenceParent semanticReferenceParent = new ReferenceParent();
       semanticReferenceParent.setKeys( List.of(key) );
       semanticReferenceParent.setType( ReferenceTypes.MODELREFERENCE );

       submodelSemanticReference.setKeys( List.of(key) );
       submodelSemanticReference.setReferredSemanticId( semanticReferenceParent );
       submodelDescriptor.setSemanticId(submodelSemanticReference);



        LangStringTextType description1 = new LangStringTextType();
        description1.setLanguage("de");
        description1.setText("hello text");
        LangStringTextType description2 = new LangStringTextType();
        description2.setLanguage("en");
        description2.setText("hello s");

        LangStringNameType displayName = new LangStringNameType();
        displayName.setLanguage( "en" );
        displayName.setText( "this is submodel display name" );



        ProtocolInformation protocolInformation = new ProtocolInformation();
        protocolInformation.setEndpointProtocol("endpointProtocolExample");

        protocolInformation.setHref("endpointAddressExample");

        protocolInformation.setEndpointProtocolVersion(List.of("e"));

        protocolInformation.setSubprotocol("subprotocolExample");
        protocolInformation.setSubprotocolBody("subprotocolBodyExample");
        protocolInformation.setSubprotocolBodyEncoding("subprotocolBodyExample");

       ProtocolInformationSecurityAttributes securityAttributes = new ProtocolInformationSecurityAttributes();
       securityAttributes.setType(ProtocolInformationSecurityAttributes.TypeEnum.NONE);
       securityAttributes.setKey( "Security Attribute key" );
       securityAttributes.setValue( "Security Attribute value" );
       protocolInformation.setSecurityAttributes(List.of(securityAttributes));

        Endpoint endpoint = new Endpoint();
        endpoint.setInterface("interfaceNameExample");
        endpoint.setProtocolInformation(protocolInformation);

       Key submodelExtensionKey = new Key();
       submodelExtensionKey.setType( KeyTypes.SUBMODEL );
       submodelExtensionKey.setValue( "submodelExtensionIdExample" );

       ReferenceParent sumodelExtensionParent = new ReferenceParent();
       sumodelExtensionParent.setType( ReferenceTypes.MODELREFERENCE );
       sumodelExtensionParent.setKeys( List.of(submodelExtensionKey) );

       Reference submodelExtensionRef = new Reference();
       submodelExtensionRef.setType( ReferenceTypes.MODELREFERENCE );
       submodelExtensionRef.setReferredSemanticId( sumodelExtensionParent );
       submodelExtensionRef.setKeys( List.of(submodelExtensionKey) );

       //supplemental semanticID
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

       Extension submodelExtension = new Extension();
       submodelExtension.setRefersTo( List.of(submodelExtensionRef) );
       submodelExtension.setSupplementalSemanticIds( List.of(submodelExtensionRef) );
       submodelExtension.setName( "Submodel Extension name" );
       submodelExtension.setValue( "Submodel Extension value" );
       submodelExtension.setValueType( DataTypeDefXsd.STRING );
       submodelExtension.setSemanticId( submodelExtensionRef );
       submodelDescriptor.setExtensions( List.of(submodelExtension) );

       submodelDescriptor.setSupplementalSemanticId( List.of(submodelSupplemSemanticIdReference) );

       submodelDescriptor.setDescription(List.of(description1, description2));
       submodelDescriptor.setDisplayName( List.of(displayName) );

        submodelDescriptor.setEndpoints(List.of(endpoint));

        return submodelDescriptor;
    }

    public static SpecificAssetId createSpecificAssetId(){
        SpecificAssetId specificAssetId1 = new SpecificAssetId();
        specificAssetId1.setName("identifier1KeyExample");
        specificAssetId1.setValue("identifier1ValueExample");

       Key assetIdKey = new Key();
       assetIdKey.setType( KeyTypes.BASICEVENTELEMENT );
       assetIdKey.setValue( "assetIdKey value" );

       ReferenceParent assetIdParent = new ReferenceParent();
       assetIdParent.setType( ReferenceTypes.EXTERNALREFERENCE );
       assetIdParent.setKeys( List.of(assetIdKey) );

        Reference reference = new Reference();
        reference.setType(ReferenceTypes.EXTERNALREFERENCE);
        Key key = new Key();
        key.setType(KeyTypes.SUBMODEL);
        key.setValue("key");
        reference.setKeys(List.of(key));
        reference.setReferredSemanticId( assetIdParent );

        specificAssetId1.setSupplementalSemanticIds(List.of(reference));
       specificAssetId1.setExternalSubjectId(reference  );

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

           ReferenceParent specificAssetIdRefParent = new ReferenceParent();
           specificAssetIdRefParent.setType( ReferenceTypes.EXTERNALREFERENCE );
           Key specificAssetIdParentKey = new Key();
           specificAssetIdParentKey.setValue( "specificAssetId ReferenceParent key" );
           specificAssetIdParentKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
           specificAssetIdRefParent.setKeys( List.of(specificAssetIdParentKey) );
           reference.setReferredSemanticId( specificAssetIdRefParent );

           specificAssetId1.setExternalSubjectId(reference);
        }


       Key assetIdKey = new Key();
       assetIdKey.setType( KeyTypes.BASICEVENTELEMENT );
       assetIdKey.setValue( "assetIdKey value" );

       ReferenceParent assetIdParent = new ReferenceParent();
       assetIdParent.setType( ReferenceTypes.EXTERNALREFERENCE );
       assetIdParent.setKeys( List.of(assetIdKey) );

       Reference assetIdReference = new Reference();
       assetIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
       assetIdReference.setKeys( List.of(assetIdKey) );
       assetIdReference.setReferredSemanticId( assetIdParent );

       specificAssetId1.setSemanticId( assetIdReference);
       specificAssetId1.setSupplementalSemanticIds( List.of(assetIdReference) );

       return specificAssetId1;
    }

    public static String getEncodedValue(String shellId){
       return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
    }
}