/*******************************************************************************
 * Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetKind;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetLink;
import org.eclipse.tractusx.semantics.aas.registry.model.Endpoint;
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
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestUtil {

   public static final String PUBLIC_READABLE = "PUBLIC_READABLE";

   public static AssetAdministrationShellDescriptor createCompleteAasDescriptor() {
      return createCompleteAasDescriptor( "semanticIdExample", "http://endpoint-address" );
   }

   public static AssetAdministrationShellDescriptor createCompleteAasDescriptor( String semanticId, String endpointUrl ) {
      AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = new AssetAdministrationShellDescriptor();
      LangStringNameType displayName = new LangStringNameType();
      displayName.setLanguage( "de" );
      displayName.setText( "this is an example description1" );
      assetAdministrationShellDescriptor.setDisplayName( List.of( displayName ) );
      assetAdministrationShellDescriptor.setGlobalAssetId( "globalAssetId example" );
      assetAdministrationShellDescriptor.setAssetType( "AssetType" );
      assetAdministrationShellDescriptor.setAssetKind( AssetKind.INSTANCE );
      assetAdministrationShellDescriptor.setId( "fb7ebcc2-5731-4948-aeaa-c9e9692decf5" );
      assetAdministrationShellDescriptor.setIdShort( generateValidIdShort( 10 ) );

      Reference specificAssetIdReference = new Reference();
      specificAssetIdReference.setType( ReferenceTypes.MODELREFERENCE );
      Key specificAssetIdKey = new Key();
      specificAssetIdKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
      specificAssetIdKey.setValue( "specificAssetIdReference key" );
      specificAssetIdReference.setKeys( List.of( specificAssetIdKey ) );

      Reference externalSubjectIdReference = new Reference();
      externalSubjectIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
      Key subjectIdKey = new Key();
      subjectIdKey.setType( KeyTypes.ASSETADMINISTRATIONSHELL );
      subjectIdKey.setValue( "ExternalSubject key value" );
      externalSubjectIdReference.setKeys( List.of( subjectIdKey ) );

      Key assetIdKey = new Key();
      assetIdKey.setType( KeyTypes.BASICEVENTELEMENT );
      assetIdKey.setValue( "assetIdKey value" );

      Reference assetIdReference = new Reference();
      assetIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
      assetIdReference.setKeys( List.of( assetIdKey ) );

      SpecificAssetId specificAssetId1 = new SpecificAssetId();
      specificAssetId1.setName( "identifier1KeyExample" );
      specificAssetId1.setValue( "identifier1ValueExample" );
      specificAssetId1.setSemanticId( specificAssetIdReference );
      specificAssetId1.setSupplementalSemanticIds( List.of( assetIdReference ) );
      specificAssetId1.setExternalSubjectId( externalSubjectIdReference );

      SpecificAssetId specificAssetId2 = new SpecificAssetId();
      specificAssetId2.setName( "identifier2KeyExample" );
      specificAssetId2.setValue( "identifier2ValueExample" );
      specificAssetId2.setSemanticId( specificAssetIdReference );
      specificAssetId2.setSupplementalSemanticIds( List.of( assetIdReference ) );
      specificAssetId2.setExternalSubjectId( externalSubjectIdReference );
      assetAdministrationShellDescriptor.setSpecificAssetIds( List.of( specificAssetId1, specificAssetId2 ) );

      LangStringTextType description1 = new LangStringTextType();
      description1.setLanguage( "de" );
      description1.setText( "hello text" );
      LangStringTextType description2 = new LangStringTextType();
      description2.setLanguage( "en" );
      description2.setText( "hello s" );
      assetAdministrationShellDescriptor.setDescription( List.of( description1, description2 ) );

      ProtocolInformation protocolInformation = new ProtocolInformation();
      protocolInformation.setEndpointProtocol( "endpointProtocolExample" );
      protocolInformation.setHref( endpointUrl );
      protocolInformation.setEndpointProtocolVersion( List.of( "e" ) );
      protocolInformation.setSubprotocol( "subprotocolExample" );
      protocolInformation.setSubprotocolBody( "subprotocolBodyExample" );
      protocolInformation.setSubprotocolBodyEncoding( "subprotocolBodyExample" );
      ProtocolInformationSecurityAttributes securityAttributes = new ProtocolInformationSecurityAttributes();
      securityAttributes.setType( ProtocolInformationSecurityAttributes.TypeEnum.NONE );
      securityAttributes.setKey( "Security Attribute key" );
      securityAttributes.setValue( "Security Attribute value" );
      protocolInformation.setSecurityAttributes( List.of( securityAttributes ) );

      Endpoint endpoint = new Endpoint();
      endpoint.setInterface( "interfaceNameExample" );
      endpoint.setProtocolInformation( protocolInformation );

      Reference submodelSemanticReference = new Reference();
      submodelSemanticReference.setType( ReferenceTypes.EXTERNALREFERENCE );
      Key key = new Key();
      key.setType( KeyTypes.SUBMODEL );
      key.setValue( semanticId );
      submodelSemanticReference.setKeys( List.of( key ) );

      Reference submodelSupplemSemanticIdReference = new Reference();
      submodelSupplemSemanticIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
      Key submodelSupplemSemanticIdkey = new Key();
      submodelSupplemSemanticIdkey.setType( KeyTypes.SUBMODEL );
      submodelSupplemSemanticIdkey.setValue( "supplementalsemanticIdExample value" );
      submodelSupplemSemanticIdReference.setKeys( List.of( submodelSupplemSemanticIdkey ) );

      SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();
      submodelDescriptor.setId( UUID.randomUUID().toString() );
      submodelDescriptor.setDisplayName( List.of( displayName ) );
      submodelDescriptor.setIdShort( generateValidIdShort( 10 ) );
      submodelDescriptor.setSemanticId( submodelSemanticReference );
      submodelDescriptor.setSupplementalSemanticId( List.of( submodelSupplemSemanticIdReference ) );
      submodelDescriptor.setDescription( List.of( description1, description2 ) );
      submodelDescriptor.setEndpoints( List.of( endpoint ) );
      List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
      submodelDescriptors.add( submodelDescriptor );
      assetAdministrationShellDescriptor.setSubmodelDescriptors( submodelDescriptors );
      return assetAdministrationShellDescriptor;
   }

   public static String generateValidIdShort(int length) {
       if (length < 2) {
           throw new IllegalArgumentException("Length must be at least 2");
       }

       // First character must be a letter
       String firstChar = RandomStringUtils.random(1, 0, 0, true, false);

       // Last character must be a letter, number, or underscore (not a hyphen)
       String lastChar = RandomStringUtils.random(1, 0, 0, true, true, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".toCharArray());

       // Middle characters can be letters, numbers, underscores, or hyphens
       String middleChars = length > 2 ?
               RandomStringUtils.random(length - 2, 0, 0, true, true, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-".toCharArray()) :
               "";

       return firstChar + middleChars + lastChar;
   }

   public static SubmodelDescriptor createSubmodel() {
      return createSubmodel( "semanticIdExample", "http://endpoint-address" );
   }

   public static SubmodelDescriptor createSubmodel( String semanticId, String endpointUrl ) {
      SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();
      submodelDescriptor.setId( UUID.randomUUID().toString() );
      submodelDescriptor.setIdShort( "idShortExample" );

      Reference submodelSemanticReference = new Reference();
      submodelSemanticReference.setType( ReferenceTypes.EXTERNALREFERENCE );
      Key key = new Key();
      key.setType( KeyTypes.SUBMODEL );
      key.setValue( semanticId );
      submodelSemanticReference.setKeys( List.of( key ) );

      submodelSemanticReference.setKeys( List.of( key ) );
      submodelDescriptor.setSemanticId( submodelSemanticReference );

      LangStringTextType description1 = new LangStringTextType();
      description1.setLanguage( "de" );
      description1.setText( "hello text" );
      LangStringTextType description2 = new LangStringTextType();
      description2.setLanguage( "en" );
      description2.setText( "hello s" );

      LangStringNameType displayName = new LangStringNameType();
      displayName.setLanguage( "en" );
      displayName.setText( "this is submodel display name" );

      ProtocolInformation protocolInformation = new ProtocolInformation();
      protocolInformation.setEndpointProtocol( "endpointProtocolExample" );
      protocolInformation.setHref( endpointUrl );
      protocolInformation.setEndpointProtocolVersion( List.of( "e" ) );
      protocolInformation.setSubprotocol( "subprotocolExample" );
      protocolInformation.setSubprotocolBody( "subprotocolBodyExample" );
      protocolInformation.setSubprotocolBodyEncoding( "subprotocolBodyExample" );

      ProtocolInformationSecurityAttributes securityAttributes = new ProtocolInformationSecurityAttributes();
      securityAttributes.setType( ProtocolInformationSecurityAttributes.TypeEnum.NONE );
      securityAttributes.setKey( "Security Attribute key" );
      securityAttributes.setValue( "Security Attribute value" );
      protocolInformation.setSecurityAttributes( List.of( securityAttributes ) );

      Endpoint endpoint = new Endpoint();
      endpoint.setInterface( "interfaceNameExample" );
      endpoint.setProtocolInformation( protocolInformation );

      Reference submodelSupplemSemanticIdReference = new Reference();
      submodelSupplemSemanticIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
      Key submodelSupplemSemanticIdkey = new Key();
      submodelSupplemSemanticIdkey.setType( KeyTypes.SUBMODEL );
      submodelSupplemSemanticIdkey.setValue( "supplementalsemanticIdExample value" );
      submodelSupplemSemanticIdReference.setKeys( List.of( submodelSupplemSemanticIdkey ) );

      submodelDescriptor.setSupplementalSemanticId( List.of( submodelSupplemSemanticIdReference ) );
      submodelDescriptor.setDescription( List.of( description1, description2 ) );
      submodelDescriptor.setDisplayName( List.of( displayName ) );
      submodelDescriptor.setEndpoints( List.of( endpoint ) );
      return submodelDescriptor;
   }

   public static SpecificAssetId createSpecificAssetId() {
      SpecificAssetId specificAssetId1 = new SpecificAssetId();
      specificAssetId1.setName( "identifier1KeyExample" );
      specificAssetId1.setValue( "identifier1ValueExample" );

      Reference reference = new Reference();
      reference.setType( ReferenceTypes.EXTERNALREFERENCE );
      Key key = new Key();
      key.setType( KeyTypes.SUBMODEL );
      key.setValue( "key" );
      reference.setKeys( List.of( key ) );

      specificAssetId1.setSupplementalSemanticIds( List.of( reference ) );
      specificAssetId1.setExternalSubjectId( reference );
      return specificAssetId1;
   }

   public static AssetLink createAssetLink() {
      AssetLink assetLink = new AssetLink();
      assetLink.setName( "identifier1KeyExample" );
      assetLink.setValue( "identifier1ValueExample" );
      return assetLink;
   }

   public static AssetLink createAssetLink(String name, String value) {
      AssetLink assetLink = new AssetLink();
      assetLink.setName( name );
      assetLink.setValue( value );
      return assetLink;
   }

   public static SpecificAssetId createSpecificAssetId( String name, String value, List<String> externalSubjectIds ) {
      SpecificAssetId specificAssetId1 = new SpecificAssetId();
      specificAssetId1.setName( name );
      specificAssetId1.setValue( value );

      if ( externalSubjectIds != null && !externalSubjectIds.isEmpty() ) {
         Reference reference = new Reference();
         reference.setType( ReferenceTypes.EXTERNALREFERENCE );
         List<Key> keys = new ArrayList<>();
         externalSubjectIds.forEach( externalSubjectId -> {
            Key key = new Key();
            key.setType( KeyTypes.SUBMODEL );
            key.setValue( externalSubjectId );
            keys.add( key );
         } );
         reference.setKeys( keys );
         specificAssetId1.setExternalSubjectId( reference );
      }

      Key assetIdKey = new Key();
      assetIdKey.setType( KeyTypes.BASICEVENTELEMENT );
      assetIdKey.setValue( "assetIdKey value" );

      Reference assetIdReference = new Reference();
      assetIdReference.setType( ReferenceTypes.EXTERNALREFERENCE );
      assetIdReference.setKeys( List.of( assetIdKey ) );
      specificAssetId1.setSemanticId( assetIdReference );
      specificAssetId1.setSupplementalSemanticIds( List.of( assetIdReference ) );
      return specificAssetId1;
   }

   public static String getEncodedValue( String shellId ) {
      return Base64.getUrlEncoder().encodeToString( shellId.getBytes() );
   }

   public static byte[] serialize( Object obj ) throws IOException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable( SerializationFeature.INDENT_OUTPUT );
      mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );
      mapper.writeValue( os, obj );
      return os.toByteArray();
   }

   public static AccessRule createAccessRule(
         String targetTenant, Map<String, String> mandatorySpecificAssetIds, Set<String> visibleSpecificAssetIds, Set<String> visibleSemanticIds ) {
      final AccessRulePolicy policy = new AccessRulePolicy();
      policy.setAccessRules( Set.of(
            new AccessRulePolicyValue( AccessRulePolicy.BPN_RULE_NAME, PolicyOperator.EQUALS, targetTenant, null ),
            new AccessRulePolicyValue( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null,
                  mandatorySpecificAssetIds.entrySet().stream()
                        .map( entry -> new AccessRulePolicyValue( entry.getKey(), PolicyOperator.EQUALS, entry.getValue(), null ) )
                        .collect( Collectors.toSet() ) ),
            new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, PolicyOperator.INCLUDES, null,
                  visibleSpecificAssetIds.stream()
                        .map( id -> new AccessRulePolicyValue( "name", PolicyOperator.EQUALS, id, null ) )
                        .collect( Collectors.toSet() ) ),
            new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SEMANTIC_IDS_RULE_NAME, PolicyOperator.INCLUDES, null,
                  visibleSemanticIds.stream()
                        .map( id -> new AccessRulePolicyValue( "modelUrn", PolicyOperator.EQUALS, id, null ) )
                        .collect( Collectors.toSet() ) )
      ) );
      AccessRule accessRule = new AccessRule();
      accessRule.setTid( "owner" );
      accessRule.setTargetTenant( targetTenant );
      accessRule.setPolicyType( AccessRule.PolicyType.AAS );
      accessRule.setPolicy( policy );
      return accessRule;
   }
}