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

import static org.eclipse.tractusx.semantics.registry.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.LangStringTextType;
import org.eclipse.tractusx.semantics.aas.registry.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.aas.registry.model.SubmodelDescriptor;
import org.eclipse.tractusx.semantics.registry.service.ShellService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AssetAdministrationShellApiTest extends AbstractAssetAdministrationShellApi {

   @Nested
   @DisplayName("Shell CRUD API")
   class ShellAPITests {


      @Test
      public void testCreateShellExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );

         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         AssetAdministrationShellDescriptor onlyRequiredFieldsShell = TestUtil.createCompleteAasDescriptor();
         onlyRequiredFieldsShell.setId( UUID.randomUUID().toString() );

         performShellCreateRequest( mapper.writeValueAsString( onlyRequiredFieldsShell ) );

      }

      @Test
      public void testCreateShellExpectRegexError() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );

         //set assetType wrong value according to regex pattern
         shellPayload.setAssetType( "AssetType \u0000" );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post(SHELL_BASE_PATH)
                           .accept(MediaType.APPLICATION_JSON)
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(mapper.writeValueAsString( shellPayload ))
                           .with(jwtTokenFactory.allRoles())
               )
               .andDo(MockMvcResultHandlers.print())
               .andExpect(status().isBadRequest())
               .andExpect( jsonPath( "$.messages[0].text", is( "must match \"^[\\x09\\x0A\\x0D\\x20-\\uD7FF\\uE000-\\uFFFD\\x{00010000}-\\x{0010FFFF}]*$\"" ) ) );
      }

      @Test
      public void testCreateShellWithExistingIdExpectBadRequest() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SHELL_BASE_PATH )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( shellPayload ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isBadRequest() )
               .andExpect( jsonPath( "$.messages[0].text", is( "An AssetAdministrationShell for the given identification does already exists." ) ) );
      }

      @Test
      public void testGetShellExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         String expectedPayload = mapper.writeValueAsString( shellPayload );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, getEncodedValue( shellId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
         .andExpect(content().json(expectedPayload));
      }

      @Test
      public void testGetShellExpectNotFound() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, "NotExistingShellId" )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() );
      }

      @Test
      public void testGetAllShellsExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SHELL_BASE_PATH )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .queryParam( "limit", "100" )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.result" ).exists() );
      }

      @Test
      public void testUpdateShellExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         shellPayload.getDisplayName().get( 0 ).setLanguage( "fr" );

         String shellId = shellPayload.getId();
         shellPayload.setIdShort( RandomStringUtils.random(10, true, true) );
         shellPayload.getSubmodelDescriptors().get( 0 ).setIdShort( RandomStringUtils.random(10, true, true) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SHELL_BASE_PATH, getEncodedValue(shellId) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( shellPayload ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, getEncodedValue( shellId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.displayName[0].language", is("fr") ) );
      }

      @Test
      public void testUpdateShellExpectNotFound() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SHELL_BASE_PATH, getEncodedValue("shellIdthatdoesnotexists") )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( shellPayload ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Shell for identifier shellIdthatdoesnotexists not found" ) ) );
      }

      @Test
      public void testUpdateShellWithDifferentIdInPayloadExpectPathIdIsTaken() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();

         String changedID = UUID.randomUUID().toString();
         shellPayload.setId( changedID );
         shellPayload.setIdShort( "newIdShortInUpdateRequest" );

         shellPayload.getSubmodelDescriptors().get( 0 ).setIdShort( RandomStringUtils.random(10, true, true) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SHELL_BASE_PATH, getEncodedValue(shellId) )
                           .accept( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( shellPayload ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         // verify that anything expect the identification can be updated
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, getEncodedValue( shellId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.id", is( shellId ) ) );
      }

      @Test
      public void testDeleteShellExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( SINGLE_SHELL_BASE_PATH, getEncodedValue(shellId) )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );
      }

      @Test
      public void testDeleteShellExpectNotFound() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( SINGLE_SHELL_BASE_PATH, getEncodedValue(shellId) )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );
      }

      /**
       * It must be possible to create multiple specificAssetIds for the same key.
       */
      @Test
      public void testCreateShellWithSameSpecificAssetIdKeyButDifferentValuesExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         shellPayload.setSpecificAssetIds( null );

         SpecificAssetId specificAssetId1 = new SpecificAssetId();
         specificAssetId1.setName( "WMI" );
         specificAssetId1.setValue( "identifier1ValueExample" );

         SpecificAssetId specificAssetId2 = new SpecificAssetId();
         specificAssetId2.setName( "WMI" );
         specificAssetId2.setValue( "identifier2ValueExample" );
         shellPayload.setSpecificAssetIds( List.of( specificAssetId1, specificAssetId2 ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SHELL_BASE_PATH )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( shellPayload ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isCreated() )
        .andExpect(content().json(mapper.writeValueAsString(shellPayload)));
      }
   }

   @Nested
   @DisplayName( "Shell SpecificAssetId CRUD API" )
   class SpecificAssetIdAPITests {

      @Test
      public void testCreateSpecificAssetIdsExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );
         String shellId = shellPayload.getId();
         ArrayNode specificAssetIds = emptyArrayNode()
               .add( specificAssetId( "key1", "value1" ) )
               .add( specificAssetId( "key2", "value2" ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SINGLE_LOOKUP_SHELL_BASE_PATH, getEncodedValue(shellId ) )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( toJson( specificAssetIds ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isCreated() )
               .andExpect( content().json( toJson( specificAssetIds ) ) );
      }

      /**
       * The API method for creation of specificAssetIds accepts an array of objects.
       * Invoking the API adds the new specificAssetIds to existing ones.
       */
      @Test
      public void testCreateSpecificAssetIdsAddToExistingSpecificAssetIdsExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();

         ArrayNode specificAssetIds = emptyArrayNode()
               .add( specificAssetId( "key1", "value1" ) )
               .add( specificAssetId( "key2", "value2" ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SINGLE_LOOKUP_SHELL_BASE_PATH, getEncodedValue( shellId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( toJson( specificAssetIds ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isCreated() )
          .andExpect(content().json(toJson(specificAssetIds)));

         // verify that the shell payload does no longer contain the initial specificAssetIds that were provided at creation time
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, getEncodedValue( shellId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
          .andExpect(jsonPath("$.specificAssetIds", hasSize(4)));
      }

      @Test
      public void testCreateSpecificIdsExpectNotFound() throws Exception {
         ArrayNode specificAssetIds = emptyArrayNode()
               .add( specificAssetId( "key1", "value1" ) );
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SINGLE_LOOKUP_SHELL_BASE_PATH,getEncodedValue("notexistingshell" )  )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( toJson( specificAssetIds ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Shell for identifier notexistingshell not found" ) ) );
      }

      @Test
      public void testGetSpecificAssetIdsExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_LOOKUP_SHELL_BASE_PATH, getEncodedValue( shellId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
         .andExpect(content().json(mapper.writeValueAsString(shellPayload.getSpecificAssetIds())));
      }

      @Test
      public void testGetSpecificIdsExpectNotFound() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_LOOKUP_SHELL_BASE_PATH, getEncodedValue("notexistingshell" ), getEncodedValue("notexistingsubmodel" ) )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Shell for identifier notexistingshell not found" ) ) );
      }
   }

   @Nested
   @DisplayName( "Submodel CRUD API" )
   class SubmodelApiTest {

      @Test
      public void testCreateSubmodelExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();

         SubmodelDescriptor submodelDescriptor = TestUtil.createSubmodel();

         performSubmodelCreateRequest( mapper.writeValueAsString( submodelDescriptor ), getEncodedValue( shellId )  );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, getEncodedValue( shellId ) )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.submodelDescriptors", hasSize( 2 ) ) )
               .andExpect(jsonPath("$.submodelDescriptors[*].id", hasItem(submodelDescriptor.getId())));
      }

      @Test
      public void testCreateSubmodelWithExistingIdExpectBadRequest() throws Exception {

         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );

         // assign submodel with existing id to shellPayload1 to ensure global uniqueness
         String shellId = shellPayload1.getId();
         SubmodelDescriptor existingSubmodel = shellPayload1.getSubmodelDescriptors().get( 0 );
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SUB_MODEL_BASE_PATH, getEncodedValue(shellId) )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( existingSubmodel ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isBadRequest() )
               .andExpect( jsonPath( "$.messages[0].text", is( ShellService.DUPLICATE_SUBMODEL_ID_SHORT_EXCEPTION ) ) );
      }

      @Test
      public void testUpdateSubModelExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );

         SubmodelDescriptor submodel = TestUtil.createSubmodel();
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ),getEncodedValue(  shellPayload1.getId()  ));
         String submodelId = submodel.getId();

         SubmodelDescriptor updatedSubmodel = TestUtil.createSubmodel();
         updatedSubmodel.setId( submodelId );
         updatedSubmodel.setIdShort( "updatedSubmodelId" );
         LangStringTextType updateDescription = new LangStringTextType();
         updateDescription.setLanguage( "cn" );
         updateDescription.setText( "chinese text" );

         updatedSubmodel.setDescription( List.of( updateDescription ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SUB_MODEL_BASE_PATH, getEncodedValue(  shellPayload1.getId()  ), getEncodedValue(  submodelId  ) )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( updatedSubmodel ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH,getEncodedValue( shellPayload1.getId() ), getEncodedValue( submodelId ))
                           .accept( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
         .andExpect(content().json(mapper.writeValueAsString( updatedSubmodel )));
      }

      @Test
      public void testUpdateSubmodelExpectNotFound() throws Exception {
         // verify shell is missing
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH, getEncodedValue( "notexistingshell") , getEncodedValue( "notexistingsubmodel")  )
                           .accept( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Shell for identifier notexistingshell not found" ) ) );

         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );
         // verify submodel is missing
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH, getEncodedValue(shellPayload1.getId()), getEncodedValue("notexistingsubmodel") )
                           .accept( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Submodel for identifier notexistingsubmodel not found." ) ) );
      }

      @Test
      public void testUpdateSubmodelWithDifferentIdInPayloadExpectPathIdIsTaken() throws Exception {
         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );
         String shellId = shellPayload1.getId();

         SubmodelDescriptor submodel = TestUtil.createSubmodel();
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ), getEncodedValue( shellPayload1.getId() ) );

         String submodelId = submodel.getId();
         submodel.setIdShort( "newIdShortInUpdateRequest" );

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SUB_MODEL_BASE_PATH, getEncodedValue( shellId ), getEncodedValue( submodelId ) )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( submodel ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         // verify that anything expect the identification can be updated
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH, getEncodedValue( shellId ), getEncodedValue( submodelId ))
                           .accept( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
         .andExpect(content().json(mapper.writeValueAsString(submodel)));
      }

      @Test
      public void testDeleteSubmodelExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );
         String shellId = shellPayload1.getId();

         SubmodelDescriptor submodel = TestUtil.createSubmodel();
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ),getEncodedValue( shellPayload1.getId())  );

         String submodelId = submodel.getId();

         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( SINGLE_SUB_MODEL_BASE_PATH,getEncodedValue( shellId) , getEncodedValue( submodelId) )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH, getEncodedValue( shellId) , getEncodedValue( submodelId))
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() );
      }

      @Test
      public void testDeleteSubmodelExpectNotFound() throws Exception {
         // verify shell is missing
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( SINGLE_SUB_MODEL_BASE_PATH, getEncodedValue( "notexistingshell" ), getEncodedValue( "notexistingsubmodel" ))
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Shell for identifier notexistingshell not found" ) ) );

         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );
         String shellId = shellPayload1.getId();
         // verify submodel is missing
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( SINGLE_SUB_MODEL_BASE_PATH,getEncodedValue( shellId) ,getEncodedValue( "notexistingsubmodel")  )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNotFound() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Submodel for identifier notexistingsubmodel not found." ) ) );
      }
   }

   @Nested
   @DisplayName( "Shell Lookup Query API" )
   class ShellLookupQueryAPI {

      @Test
      public void testLookUpApiWithInvalidQueryParameterExpectFailure() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", "{ invalid }" )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isBadRequest() )
               .andExpect( jsonPath( "$.messages[0].text", is( "Incorrect Base64 encoded value provided as parameter" ) ) );
      }

      @Test
      public void testLookUpApiWithSwaggerUIEscapedQueryParameterExpectSuccess() throws Exception {
         String swaggerUIEscapedAssetIds = "[\"{\\n  \\\"name\\\": \\\"brakenumber\\\",\\n  \\\"value\\\": \\\"123f092\\\"\\n}\",{\"name\":\"globalAssetId\",\"value\":\"12397f2kf97df\"}]";
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "aasIdentifier", swaggerUIEscapedAssetIds )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( content().string( "{}" ));
      }

      @Test
      public void testLookUpApiWithMultiParamIds() throws Exception {
         String assetId1 = "{\"name\": \"brakenumber\",\"value\": \"123f092\"}";
         String assetId2 = "{\"name\":\"globalAssetId\",\"value\":\"12397f2kf97df\"}";
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "aasIdentifier", assetId1 )
                           .queryParam( "aasIdentifier", assetId2 )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( content().string( "{}" ));
      }

      @Test
      public void testFindExternalShellIdsBySpecificAssetIdsExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );

         AssetAdministrationShellDescriptor shellPayload2 = TestUtil.createCompleteAasDescriptor();
         shellPayload2.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload2 ) );

         SpecificAssetId specificAssetId1 = TestUtil.createSpecificAssetId();
         String encodedSa1 = Base64.getUrlEncoder().encodeToString(serialize( specificAssetId1));

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", encodedSa1 )
                           .queryParam( "limit", "1" )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.paging_metadata.cursor" ).exists() );

         // Test first shell match with single assetId

         SpecificAssetId specificAssetId2 = TestUtil.createSpecificAssetId( "identifier99KeyExample", "identifier99ValueExample", null );
         String encodedSa2 = Base64.getUrlEncoder().encodeToString(serialize( specificAssetId2));
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", encodedSa2)
                           .queryParam( "limit", "10" )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
          .andExpect( jsonPath( "$.paging_metadata.cursor" ).doesNotExist() );

         //            // Test first and second shell match with common asssetId

         SpecificAssetId specificAssetId3 = TestUtil.createSpecificAssetId( "commonAssetIdKey", "commonAssetIdValue", null );
         String encodedSa3 = Base64.getUrlEncoder().encodeToString(serialize( specificAssetId3));
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", encodedSa3 )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.result", hasSize( 0 ) ) );
      }

      @Test
      public void testFindExternalShellIdByGlobalAssetIdExpectSuccess() throws Exception {

         String globalAssetId = UUID.randomUUID().toString();

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         shellPayload.setGlobalAssetId( globalAssetId );
         String payload = mapper.writeValueAsString( shellPayload );
         performShellCreateRequest(payload );

         // for lookup global asset id is handled as specificAssetIds
         SpecificAssetId SAGlobal = TestUtil.createSpecificAssetId("globalAssetId",globalAssetId,null);
         String encodedSa1 = Base64.getUrlEncoder().encodeToString(serialize( SAGlobal));


         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .queryParam( "assetIds", encodedSa1)
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.result", hasSize( 1 ) ) )
               // ensure that only three results match
               .andExpect( jsonPath( "$.result", contains( shellPayload.getId() ) ) );
      }

      @Test
      public void testFindExternalShellIdsWithoutProvidingQueryParametersExpectEmptyResult() throws Exception {
         // prepare the data set
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( content().string( "{}" ));

      }
   }

   @Nested
   @DisplayName( "Custom AAS API Tests" )
   class CustomAASApiTest {

      //@Test
      public void testCreateShellInBatchWithOneDuplicateExpectSuccess() throws Exception {
         ObjectNode shell = createShell();

         JsonNode identification = shell.get( "identification" );
         ArrayNode batchShellBody = emptyArrayNode().add( shell ).add( createShell()
               // create duplicate
               .set( "identification", identification ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SHELL_BASE_PATH + "/batch" )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( toJson( batchShellBody ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isCreated() )
               .andExpect( jsonPath( "$", hasSize( 2 ) ) )
               .andExpect( jsonPath( "$[0].message", equalTo( "AssetAdministrationShell successfully created." ) ) )
               .andExpect( jsonPath( "$[0].identification", equalTo( identification.textValue() ) ) )
               .andExpect( jsonPath( "$[0].status", equalTo( 200 ) ) )
               .andExpect( jsonPath( "$[1].message", equalTo( "An AssetAdministrationShell for the given identification does already exists." ) ) )
               .andExpect( jsonPath( "$[1].identification", equalTo( identification.textValue() ) ) )
               .andExpect( jsonPath( "$[1].status", equalTo( 400 ) ) );
      }

      // @Test
      public void testCreateShellInBatchExpectSuccess() throws Exception {
         ArrayNode batchShellBody = emptyArrayNode().add( createShell() )
               .add( createShell() )
               .add( createShell() )
               .add( createShell() )
               .add( createShell() );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SHELL_BASE_PATH + "/batch" )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( toJson( batchShellBody ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isCreated() )
               .andExpect( jsonPath( "$", hasSize( 5 ) ) );
      }

      @Test
      public void testFindExternalShellIdsBySpecificAssetIdsWithAnyMatchExpectSuccess() throws Exception {
         // the keyPrefix ensures that this test can run against a persistent database multiple times
         String keyPrefix = UUID.randomUUID().toString();
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setSpecificAssetIds(null);
         shellPayload.setId(UUID.randomUUID().toString());
         SpecificAssetId asset1 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2","value_2",null);
         SpecificAssetId asset2 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2_1","value_2_1",List.of(jwtTokenFactory.tenantTwo().getTenantId()));
         SpecificAssetId asset3 = TestUtil.createSpecificAssetId(keyPrefix + "findExternal_2_2","value_2_2",List.of(jwtTokenFactory.tenantThree().getTenantId()));

         shellPayload.setSpecificAssetIds(List.of(asset1,asset2,asset3));

         performShellCreateRequest(mapper.writeValueAsString(shellPayload));

         // query to retrieve any match (asset1 is only visible for OWNER TENANT_ONE)
         JsonNode anyMatchQueryByOwnerAssetIds = mapper.createObjectNode().set( "query", mapper.createObjectNode()
               .set( "assetIds", emptyArrayNode()
                     .add( specificAssetId( asset1.getName(), asset1.getValue() ) )
                     .add( specificAssetId( "not_available_key_in_shell","not_available_value_in_shell" ) ))
         );

         // Test with non Owner(TENANT_TWO)
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( LOOKUP_SHELL_BASE_PATH + "/query" )
                           .content( toJson( anyMatchQueryByOwnerAssetIds ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantTwo().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$", hasSize( 0 ) ) );

         // Test with owner DTR (TENANT_ONE)
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( LOOKUP_SHELL_BASE_PATH + "/query" )
                           .content( toJson( anyMatchQueryByOwnerAssetIds ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$", hasSize( 1 ) ) )
               .andExpect( jsonPath( "$[0]" ,is(shellPayload.getId()) ));

         // query to retrieve any match (asset2 is only visible for OWNER TENANT_ONE and TENANT_TWO)
         JsonNode anyMatchQueryByTenantTwoAssetIds = mapper.createObjectNode().set( "query", mapper.createObjectNode()
               .set( "assetIds", emptyArrayNode()
                     .add( specificAssetId( asset2.getName(), asset2.getValue() ) )
                     .add( specificAssetId( "not_available_key_in_shell","not_available_value_in_shell" ) ))
         );

         // Test with non Owner(TENANT_TWO)
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( LOOKUP_SHELL_BASE_PATH + "/query" )
                           .content( toJson( anyMatchQueryByTenantTwoAssetIds ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantTwo().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( jsonPath( "$", hasSize( 1 ) ) )
               .andExpect( jsonPath( "$[0]" ,is(shellPayload.getId()) ));

         // Test with non Owner(TENANT_THREE)
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( LOOKUP_SHELL_BASE_PATH + "/query" )
                           .content( toJson( anyMatchQueryByOwnerAssetIds ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantThree().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$", hasSize( 0 ) ) );

         // Test with owner DTR (TENANT_ONE)
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( LOOKUP_SHELL_BASE_PATH + "/query" )
                           .content( toJson( anyMatchQueryByTenantTwoAssetIds ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .header( EXTERNAL_SUBJECT_ID_HEADER, jwtTokenFactory.tenantOne().getTenantId() )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$", hasSize( 1 ) ) )
               .andExpect( jsonPath( "$[0]" ,is(shellPayload.getId()) ));

      }

      //@Test
      public void testFetchShellsByNoIdentificationsExpectEmptyResult() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SHELL_BASE_PATH + "/fetch" )
                           .content( toJson( emptyArrayNode() ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.items", hasSize( 0 ) ) );
      }

      //@Test
      public void testFetchShellsByMultipleIdentificationsExpectSuccessExpectSuccess() throws Exception {

         ObjectNode shellPayload1 = createShell();
         performShellCreateRequest( toJson( shellPayload1 ) );

         ObjectNode shellPayload2 = createShell();
         performShellCreateRequest( toJson( shellPayload2 ) );

         ArrayNode fetchOneShellsById = emptyArrayNode().add( getId( shellPayload1 ) );
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SHELL_BASE_PATH + "/fetch" )
                           .content( toJson( fetchOneShellsById ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.items", hasSize( 1 ) ) )
               // ensure that only three results match
               .andExpect( jsonPath( "$.items[*].identification", hasItem( getId( shellPayload1 ) ) ) );

         ArrayNode fetchTwoShellsById = emptyArrayNode()
               .add( getId( shellPayload1 ) )
               .add( getId( shellPayload2 ) );
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SHELL_BASE_PATH + "/fetch" )
                           .content( toJson( fetchTwoShellsById ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.items", hasSize( 2 ) ) )
               // ensure that only three results match
               .andExpect( jsonPath( "$.items[*].identification",
                     hasItems( getId( shellPayload1 ), getId( shellPayload2 ) ) ) );
      }
   }

   @Test
   @DisplayName( "Test creating a new Asset Administration Shell Descriptor with unique IdShort in shell and submodelDescriptor level" )
   public void test_Creating_a_new_Asset_Administration_Shell_Descriptor_with_unique_IdShort_in_shell_and_submodelDescriptor_level() throws Exception {
      //Given
      AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
      //When & Then
      mvc.perform(
                  MockMvcRequestBuilders
                        .post( SHELL_BASE_PATH )
                        .accept( MediaType.APPLICATION_JSON )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( shellPayload ) )
                        .with( jwtTokenFactory.allRoles() )
            )
            .andDo( MockMvcResultHandlers.print() )
            .andExpect( status().isCreated());
   }

   @Test
   @DisplayName( "Test creating a new Asset Administration Shell Descriptor with dupilcate IdShort in shell level" )
   public void test_Creating_a_new_Asset_Administration_Shell_Descriptor_with_Dupilcate_IdShort_in_shell_level() throws Exception {
      //Given
      //Creates a shell using test data
      AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
      shellPayload.setId( UUID.randomUUID().toString() );
      performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );
      String idShort = shellPayload.getIdShort();

      //Creating a new shell with unique Shell.IdShort and Shell.SubmodelDescriptor.IdShort
      shellPayload = TestUtil.createCompleteAasDescriptor();
      //setting duplicate idShort is shell level
      shellPayload.setIdShort( idShort );

      //When & Then
      mvc.perform(
                  MockMvcRequestBuilders
                        .post( SHELL_BASE_PATH )
                        .accept( MediaType.APPLICATION_JSON )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( shellPayload ) )
                        .with( jwtTokenFactory.allRoles() )
            )
            .andDo( MockMvcResultHandlers.print() )
            .andExpect( status().isBadRequest() )
            .andExpect( jsonPath( "$.messages[0].text", is( "An AssetAdministrationShell for the given IdShort already exists." ) ) );
   }

   @Test
   @DisplayName( "Test Creating a new Asset Administration Shell Descriptor with unique  IdShort in shell level and duplicate IdShort in submodelDescriptor level" )
   public void test_Creating_a_new_Asset_Administration_Shell_Descriptor_with_unique_IdShort_in_shell_level_and_duplicate_submodelDescriptor_level() throws Exception {
      //Given
      AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();

      SubmodelDescriptor submodelDescriptor = new SubmodelDescriptor();
      //Setting duplicate id short
      submodelDescriptor.setIdShort(shellPayload.getSubmodelDescriptors().get( 0 ).getIdShort());

      //Adding duplicate submodel which contains duplicate idshort
      shellPayload.getSubmodelDescriptors().add( submodelDescriptor);


      //When & Then
      mvc.perform(
                  MockMvcRequestBuilders
                        .post( SHELL_BASE_PATH )
                        .accept( MediaType.APPLICATION_JSON )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( shellPayload ) )
                        .with( jwtTokenFactory.allRoles() )
            )
            .andDo( MockMvcResultHandlers.print() )
            .andExpect( status().isBadRequest() )
            .andExpect( jsonPath( "$.messages[0].text", is( ShellService.DUPLICATE_SUBMODEL_ID_SHORT_EXCEPTION ) ) );
   }

   @Test
   @DisplayName( "Test Creating a new Submodel Descriptor with unique IdShort in submodelDescriptor level" )
   public void test_Creates_a_new_Submodel_Descriptor_with_unique_IdShort_in_submodelDescriptor_level() throws Exception {
      //Given
      AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
      shellPayload1.setId( UUID.randomUUID().toString() );
      performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );

      String shellId = shellPayload1.getId();
      //Get new SubmodelDescriptor which contains unique IdShort
      SubmodelDescriptor submodel = TestUtil.createCompleteAasDescriptor().getSubmodelDescriptors().get( 0 );
      mvc.perform(
                  MockMvcRequestBuilders
                        .post( SUB_MODEL_BASE_PATH, getEncodedValue(shellId) )
                        .accept( MediaType.APPLICATION_JSON )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( submodel ) )
                        .with( jwtTokenFactory.allRoles() )
            )
            .andDo( MockMvcResultHandlers.print() )
            .andExpect( status().isCreated());
   }

   @Test
   @DisplayName( "Test Creating a new Submodel Descriptor with duplicate IdShort in submodelDescriptor level - DB" )
   public void test_Creates_a_new_Submodel_Descriptor_with_duplicate_IdShort_in_submodelDescriptor_level_in_DB() throws Exception {
      //Given
      AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
      shellPayload1.setId( UUID.randomUUID().toString() );
      performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );

      String shellId = shellPayload1.getId();
      SubmodelDescriptor submodel = shellPayload1.getSubmodelDescriptors().get( 0 );
      submodel.setId( UUID.randomUUID().toString() );

      //When & Then
      mvc.perform(
                  MockMvcRequestBuilders
                        .post( SUB_MODEL_BASE_PATH, getEncodedValue(shellId) )
                        .accept( MediaType.APPLICATION_JSON )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( mapper.writeValueAsString( submodel ) )
                        .with( jwtTokenFactory.allRoles() )
            )
            .andDo( MockMvcResultHandlers.print() )
            .andExpect( status().isBadRequest() )
            .andExpect( jsonPath( "$.messages[0].text", is( ShellService.DUPLICATE_SUBMODEL_ID_SHORT_EXCEPTION ) ) );
   }


   @Nested
   @DisplayName( "Description Tests" )
   class DescriptionApiTest {
      @Test
      public void testGetDescriptionExpectSuccess() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( "/api/v3.0/description" )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.profiles[0]", is( "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRegistryServiceSpecification/SSP-001" ) ) );
      }
   }

}