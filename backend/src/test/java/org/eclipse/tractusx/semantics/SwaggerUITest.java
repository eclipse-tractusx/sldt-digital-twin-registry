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
package org.eclipse.tractusx.semantics;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SwaggerUITest {
   @Autowired
   private MockMvc mockMvc;

   @Test
   public void testGetSwaggerUiExpect200() throws Exception {
      this.mockMvc.perform( get( "/swagger-ui/index.html" ) )
                  .andDo( print() )
                  .andExpect( status().isOk() )
                  .andExpect( content().string( containsString( "<div id=\"swagger-ui\"></div>" ) ) );
   }

   @Test
   public void testGetRootExpectRedirectedToSwaggerUI() throws Exception {
      this.mockMvc.perform( get( "/" ) )
                  .andDo( print() )
                  .andExpect( status().isFound() )
                  .andExpect( redirectedUrl( "/swagger-ui/index.html" ) );
   }
}
