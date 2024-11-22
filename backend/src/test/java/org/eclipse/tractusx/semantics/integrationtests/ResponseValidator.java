/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.eclipse.tractusx.semantics.integrationtests.model.Assertion;
import org.eclipse.tractusx.semantics.integrationtests.model.ExpectedResponse;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseValidator {

   private final ObjectMapper objectMapper = new ObjectMapper();

   public void validateResponse( ResultActions actual, ExpectedResponse expectedResponse ) throws Exception {
      actual.andExpect( status().is( expectedResponse.getStatus() ) );

      if ( expectedResponse.isContent() ) {
         if ( expectedResponse.getExpectedPayload() != null ) {
            actual.andExpect( content().json( objectMapper.writeValueAsString( expectedResponse.getExpectedPayload() ) ) );
         }
         executeAssertions( actual, expectedResponse.getAssertions() );
      }
   }

   private void executeAssertions( ResultActions actual, List<Assertion> assertions ) throws Exception {
      for ( Assertion assertion : assertions ) {
         if ( assertion.getExists() != null && assertion.getExists() ) {
            actual.andExpect( jsonPath( assertion.getJsonPath() ).exists() );
         } else if ( assertion.getEquals() != null ) {
            actual.andExpect( jsonPath( assertion.getJsonPath(), equalTo( assertion.getEquals() ) ) );
         } else if ( assertion.getHasSize() != null ) {
            actual.andExpect( jsonPath( assertion.getJsonPath(), hasSize( assertion.getHasSize() ) ) );
         } else if ( assertion.getContains() != null ) {
            actual.andExpect( jsonPath( assertion.getJsonPath(), contains( assertion.getContains() ) ) );
         } else if ( assertion.getDoesNotExist() != null && assertion.getDoesNotExist() ) {
            actual.andExpect( jsonPath( assertion.getJsonPath() ).doesNotExist() );
         } else if ( assertion.getHasItem() != null ) {
            actual.andExpect( jsonPath( assertion.getJsonPath(), hasItem( assertion.getHasItem() ) ) );
         } else if ( assertion.getIsEmpty() != null && assertion.getIsEmpty() ) {
            actual.andExpect( jsonPath( assertion.getJsonPath() ).isEmpty() );
         } else if ( assertion.getIsNotEmpty() != null && assertion.getIsNotEmpty() ) {
            actual.andExpect( jsonPath( assertion.getJsonPath() ).isNotEmpty() );
         }
      }
   }
}
