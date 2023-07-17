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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.LangStringTextType;
import org.eclipse.tractusx.semantics.aas.registry.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.aas.registry.model.SubmodelDescriptor;
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

         AssetAdministrationShellDescriptor onlyRequiredFieldsShell = new AssetAdministrationShellDescriptor();
         onlyRequiredFieldsShell.setId( UUID.randomUUID().toString() );

         performShellCreateRequest( mapper.writeValueAsString( onlyRequiredFieldsShell ) );

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
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
       //  .andExpect(content().json(toJson(shellPayload)));
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

         shellPayload.getDescription().get( 0 ).setLanguage( "fr" );

         String shellId = shellPayload.getId();

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( shellPayload ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
         //.andExpect(content().json(toJson(updateDescription)));
      }

      @Test
      public void testUpdateShellExpectNotFound() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SHELL_BASE_PATH, "shellIdthatdoesnotexists" )
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

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( shellPayload ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         // verify that anything expect the identification can be updated
         shellPayload.setId( UUID.randomUUID().toString() );
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
         //.andExpect(content().json(toJson(expectedShellAfterUpdate)));
      }

      @Test
      public void testDeleteShellExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();
         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( SINGLE_SHELL_BASE_PATH, shellId )
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
                           .delete( SINGLE_SHELL_BASE_PATH, shellId )
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
               .andExpect( status().isCreated() );
         //.andExpect(content().json(mapper.writeValueAsString(shellPayload)));
      }
   }

   @Nested
   @DisplayName( "Shell SpecificAssetId CRUD API" )
   class SpecificAssetIdAPITests {

      @Test
      public void testCreateSpecificAssetIdsExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         shellPayload.setSpecificAssetIds( null );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );
         String shellId = shellPayload.getId();
         ArrayNode specificAssetIds = emptyArrayNode()
               .add( specificAssetId( "key1", "value1" ) )
               .add( specificAssetId( "key2", "value2" ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SINGLE_LOOKUP_SHELL_BASE_PATH, shellId )
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
       * Invoking the API removes all existing specificAssetIds and adds the new ones.
       */
      @Test
      public void testCreateSpecificAssetIdsReplacesAllExistingSpecificAssetIdsExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         String shellId = shellPayload.getId();

         ArrayNode specificAssetIds = emptyArrayNode()
               .add( specificAssetId( "key1", "value1" ) )
               .add( specificAssetId( "key2", "value2" ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SINGLE_LOOKUP_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( toJson( specificAssetIds ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isCreated() );
         // .andExpect(content().json(toJson(specificAssetIds)));

         // verify that the shell payload does no longer contain the initial specificAssetIds that were provided at creation time
         // ObjectNode expectedShellPayload = shellPayload.set("specificAssetIds", specificAssetIds);
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
         // .andExpect(content().json(toJson(expectedShellPayload)));
      }

      @Test
      public void testCreateSpecificIdsExpectNotFound() throws Exception {
         ArrayNode specificAssetIds = emptyArrayNode()
               .add( specificAssetId( "key1", "value1" ) );
         mvc.perform(
                     MockMvcRequestBuilders
                           .post( SINGLE_LOOKUP_SHELL_BASE_PATH, "notexistingshell" )
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
                           .get( SINGLE_LOOKUP_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
         //.andExpect(content().json(toJson(shellPayload.get("specificAssetIds")));
      }

      @Test
      public void testGetSpecificIdsExpectNotFound() throws Exception {
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_LOOKUP_SHELL_BASE_PATH, "notexistingshell", "notexistingsubmodel" )
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

         performSubmodelCreateRequest( mapper.writeValueAsString( submodelDescriptor ), shellId );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SHELL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$.submodelDescriptors", hasSize( 2 ) ) );
         //                    .andExpect(jsonPath("$.submodelDescriptors[*].identification", hasItem(getId(submodel))));
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
                           .post( SUB_MODEL_BASE_PATH, shellId )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( existingSubmodel ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isBadRequest() )
               .andExpect( jsonPath( "$.messages[0].text", is( "A SubmodelDescriptor with the given identification does already exists." ) ) );
      }

      @Test
      public void testUpdateSubModelExpectSuccess() throws Exception {
         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );

         SubmodelDescriptor submodel = TestUtil.createSubmodel();
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ), shellPayload1.getId() );
         String submodelId = submodel.getId();

         SubmodelDescriptor updatedSubmodel = TestUtil.createSubmodel();
         updatedSubmodel.setIdShort( "updatedSubmodelId" );
         LangStringTextType updateDescription = new LangStringTextType();
         updateDescription.setLanguage( "cn" );
         updateDescription.setText( "chinese text" );

         updatedSubmodel.setDescription( List.of( updateDescription ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SUB_MODEL_BASE_PATH, shellPayload1.getId(), submodelId )
                           .accept( MediaType.APPLICATION_JSON )
                           .contentType( MediaType.APPLICATION_JSON )
                           .content( mapper.writeValueAsString( updatedSubmodel ) )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH, shellPayload1.getId(), submodelId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
         //.andExpect(content().json(toJson(updatedSubmodel)));
      }

      @Test
      public void testUpdateSubmodelExpectNotFound() throws Exception {
         // verify shell is missing
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH, "notexistingshell", "notexistingsubmodel" )
                           .accept( MediaType.APPLICATION_JSON )
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
                           .get( SINGLE_SUB_MODEL_BASE_PATH, shellPayload1.getId(), "notexistingsubmodel" )
                           .accept( MediaType.APPLICATION_JSON )
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
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ), shellPayload1.getId() );

         String submodelId = submodel.getId();
         submodel.setIdShort( "newIdShortInUpdateRequest" );

         mvc.perform(
                     MockMvcRequestBuilders
                           .put( SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
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
                           .get( SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );
         //.andExpect(content().json(mapper.writeValueAsString(submodel)));
      }

      @Test
      public void testDeleteSubmodelExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );
         String shellId = shellPayload1.getId();

         SubmodelDescriptor submodel = TestUtil.createSubmodel();
         performSubmodelCreateRequest( mapper.writeValueAsString( submodel ), shellPayload1.getId() );

         String submodelId = submodel.getId();

         mvc.perform(
                     MockMvcRequestBuilders
                           .delete( SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isNoContent() );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( SINGLE_SUB_MODEL_BASE_PATH, shellId, submodelId )
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
                           .delete( SINGLE_SUB_MODEL_BASE_PATH, "notexistingshell", "notexistingsubmodel" )
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
                           .delete( SINGLE_SUB_MODEL_BASE_PATH, shellId, "notexistingsubmodel" )
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
               .andExpect( jsonPath( "$.messages[0].text", is( "The provided parameters are invalid. assetIds={ invalid }" ) ) );
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
               .andExpect( jsonPath( "$" ).isArray() );
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
               .andExpect( jsonPath( "$" ).isArray() );
      }

      @Test
      public void testFindExternalShellIdsBySpecificAssetIdsExpectSuccess() throws Exception {

         AssetAdministrationShellDescriptor shellPayload1 = TestUtil.createCompleteAasDescriptor();
         shellPayload1.setId( UUID.randomUUID().toString() );
         performShellCreateRequest( mapper.writeValueAsString( shellPayload1 ) );

         SpecificAssetId specificAssetId1 = TestUtil.createSpecificAssetId();
         JsonNode node = mapper.createObjectNode()
               .set( "specificAssetId", emptyArrayNode()
                     .add( specificAssetId( "findExternalShellIdQueryKey_1", "value_1" ) )
                     .add( specificAssetId( "findExternalShellIdQueryKey_2", "value_2" ) ) );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", mapper.writeValueAsString( specificAssetId1 ) )
                           .queryParam( "limit", "10" )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );

         // Test first shell match with single assetId

         SpecificAssetId specificAssetId2 = TestUtil.createSpecificAssetId( "identifier1KeyExample", "identifier1ValueExample", null );
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", mapper.writeValueAsString( specificAssetId2 ) )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() );

         //            // Test first and second shell match with common asssetId

         SpecificAssetId specificAssetId3 = TestUtil.createSpecificAssetId( "commonAssetIdKey", "commonAssetIdValue", null );

         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", mapper.writeValueAsString( specificAssetId3 ) )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$", hasSize( 0 ) ) );
      }

      @Test
      public void testFindExternalShellIdByGlobalAssetIdExpectSuccess() throws Exception {

         String globalAssetId = UUID.randomUUID().toString();

         AssetAdministrationShellDescriptor shellPayload = TestUtil.createCompleteAasDescriptor();
         shellPayload.setGlobalAssetId( globalAssetId );
         String payload = mapper.writeValueAsString( shellPayload );
         performShellCreateRequest(payload );
         //performShellCreateRequest( mapper.writeValueAsString( shellPayload ) );

         // for lookup global asset id is handled as specificAssetIds
         ArrayNode globalAssetIdForSampleQuery = emptyArrayNode().add(
               specificAssetId( "globalAssetId", globalAssetId )
         );
         mvc.perform(
                     MockMvcRequestBuilders
                           .get( LOOKUP_SHELL_BASE_PATH )
                           .queryParam( "assetIds", toJson( globalAssetIdForSampleQuery ) )
                           .accept( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$", hasSize( 1 ) ) )
               // ensure that only three results match
               .andExpect( jsonPath( "$", contains( shellPayload.getId() ) ) );
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
               .andExpect( jsonPath( "$", hasSize( 0 ) ) );
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

      //@Test
      public void testFindExternalShellIdsBySpecificAssetIdsWithAnyMatchExpectSuccess() throws Exception {
         // the keyPrefix ensures that this test can run against a persistent database multiple times
         String keyPrefix = UUID.randomUUID().toString();
         ObjectNode commonAssetId = specificAssetId( keyPrefix + "commonAssetIdKey", "commonAssetIdValue" );
         // first shell
         ObjectNode firstShellPayload = createBaseIdPayload( "sampleForQuery", "idShortSampleForQuery" );
         firstShellPayload.set( "specificAssetIds", emptyArrayNode()
               .add( specificAssetId( keyPrefix + "findExternalShellIdQueryKey_1", "value_1" ) ) );
         performShellCreateRequest( toJson( firstShellPayload ) );

         // second shell
         ObjectNode secondShellPayload = createBaseIdPayload( "sampleForQuery", "idShortSampleForQuery" );
         secondShellPayload.set( "specificAssetIds", emptyArrayNode()
               .add( specificAssetId( keyPrefix + "findExternalShellIdQueryKey_2", "value_2" ) ) );
         performShellCreateRequest( toJson( secondShellPayload ) );

         // query to retrieve any match
         JsonNode anyMatchAueryByAssetIds = mapper.createObjectNode().set( "query", mapper.createObjectNode()
               .set( "assetIds", emptyArrayNode()
                     .add( specificAssetId( keyPrefix + "findExternalShellIdQueryKey_1", "value_1" ) )
                     .add( specificAssetId( keyPrefix + "findExternalShellIdQueryKey_2", "value_2" ) )
                     .add( commonAssetId ) )
         );

         mvc.perform(
                     MockMvcRequestBuilders
                           .post( LOOKUP_SHELL_BASE_PATH + "/query" )
                           .content( toJson( anyMatchAueryByAssetIds ) )
                           .contentType( MediaType.APPLICATION_JSON )
                           .with( jwtTokenFactory.allRoles() )
               )
               .andDo( MockMvcResultHandlers.print() )
               .andExpect( status().isOk() )
               .andExpect( jsonPath( "$", hasSize( 2 ) ) )
               .andExpect( jsonPath( "$", containsInAnyOrder( getId( firstShellPayload ), getId( secondShellPayload ) ) ) );
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

}