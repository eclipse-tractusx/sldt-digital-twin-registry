/*******************************************************************************
 * Copyright (c) 2025 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.semantics.integrationtests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.integrationtests.model.ApiRequest;
import org.eclipse.tractusx.semantics.integrationtests.model.ExpectedResponse;
import org.eclipse.tractusx.semantics.registry.JwtTokenFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties( RegistryProperties.class )
public class AssetAdministrationShellIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   protected JwtTokenFactory jwtTokenFactory;

   private final ResponseValidator responseValidator = new ResponseValidator();

   private final ObjectMapper objectMapper = new ObjectMapper();
   private static final String ROOT_DIR = "src/test/resources/integrationtests/";
   private static final List<String> LIST_OF_TEST_CASES_DIR = List.of( "aas-registry-usecases" );
   private static final String REQUEST_FILE_NAME = "request.json";
   private static final String EXPECTED_RESPONSE_FILE_NAME = "expected-response.json";
   protected static final String EXTERNAL_SUBJECT_ID_HEADER = "Edc-Bpn";

   @TestFactory
   List<DynamicTest> executeTests() {

      List<DynamicTest> dynamicTests = new ArrayList<>();

      LIST_OF_TEST_CASES_DIR.forEach( testcasePath -> {
         try {
            Stream<DynamicTest> tests = Files.list( Paths.get( ROOT_DIR + testcasePath ) )
                  .filter( Files::isDirectory )
                  .map( useCasePath -> {
                     try {
                        return DynamicTest.dynamicTest( testcasePath + "_" + useCasePath.getFileName().toString(),
                              () -> executeUseCaseTest( useCasePath ) );
                     } catch ( Exception e ) {
                        throw new RuntimeException( "Failed to load usecase:" + useCasePath, e );
                     }
                  } );

            dynamicTests.addAll( tests.toList() );
         } catch ( IOException e ) {
            throw new RuntimeException( "Failed to load test steps", e );
         }
      } );

      return dynamicTests;
   }

   private void executeUseCaseTest( Path useCasePath ) throws Exception {
      Files.list( useCasePath )
            .filter( Files::isDirectory )
            .sorted( Comparator.comparing( Path::getFileName ) )
            .forEach( testStepPath -> {
               try {
                     executeTestStep( testStepPath );
               } catch ( Exception e ) {
                  throw new RuntimeException( "Failed to load test steps", e );
               }
            } );
   }

   private void executeTestStep( Path testStepPath ) throws Exception {
      // Load request.json
      File requestFile = testStepPath.resolve( REQUEST_FILE_NAME ).toFile();
      ApiRequest request = objectMapper.readValue( requestFile, ApiRequest.class );

      // Load expected-response.json
      File expectedResponseFile = testStepPath.resolve( EXPECTED_RESPONSE_FILE_NAME ).toFile();
      ExpectedResponse expectedResponse = objectMapper.readValue( expectedResponseFile, ExpectedResponse.class );

      // Perform request
      ResultActions actual = performRequest( request );

      // Validate response
      responseValidator.validateResponse( actual, expectedResponse );
   }

   private ResultActions performRequest( ApiRequest request ) throws Exception {
      HttpMethod httpMethod = HttpMethod.valueOf( request.getMethod().toUpperCase() );
      MockHttpServletRequestBuilder requestBuilder = createRequestBuilder( httpMethod, request );
      JwtTokenFactory.Tenant tenant = jwtTokenFactory.getTenant( request.getTenant() );
      return mockMvc.perform(
            requestBuilder
                  .header( EXTERNAL_SUBJECT_ID_HEADER, tenant.getTenantId() )
                  .accept( MediaType.APPLICATION_JSON )
                  .with( tenant.allRoles() )
      );
   }

   private MockHttpServletRequestBuilder createRequestBuilder( HttpMethod httpMethod, ApiRequest request ) throws JsonProcessingException {
      String requestUrl = request.getUrl();
      switch ( httpMethod.name() ) {
      case "GET":
         return get( requestUrl );
      case "POST":
         return post( requestUrl )
               .contentType( MediaType.APPLICATION_JSON )
               .content( objectMapper.writeValueAsString( request.getBody() ) );
      case "PUT":
         return put( requestUrl )
               .contentType( MediaType.APPLICATION_JSON )
               .content( objectMapper.writeValueAsString( request.getBody() ) );
      case "DELETE":
         return delete( requestUrl );
      default:
         throw new UnsupportedOperationException( "HTTP method not supported: " + request.getMethod() );
      }
   }
}
