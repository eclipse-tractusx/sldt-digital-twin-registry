/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2024 Draexlmaier Group
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

import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(profiles = { "cognito-test" })
public class CognitoApiSecurityTest extends AbstractAssetAdministrationShellApi {
    @Test
    public void testWithInvalidAuthenticationTokenConfigurationExpectUnauthorized() throws Exception {
       mvc.perform(
                   MockMvcRequestBuilders
                         .get( SINGLE_SHELL_BASE_PATH, UuidCreator.getTimeOrderedEpoch() )
                         .accept( MediaType.APPLICATION_JSON )
                         .with( jwtTokenFactory.withoutResourceAccess() )
             )
             .andDo( MockMvcResultHandlers.print() )
             .andExpect( status().isForbidden() );

       mvc.perform(
                   MockMvcRequestBuilders
                         .get( SINGLE_SHELL_BASE_PATH, UuidCreator.getTimeOrderedEpoch() )
                         .accept( MediaType.APPLICATION_JSON )
                         .with( jwtTokenFactory.withoutRoles() )
             )
             .andDo( MockMvcResultHandlers.print() )
             .andExpect( status().isForbidden() );
    }

    @Test
    public void testWithAuthenticationTokenConfigurationExpectAuthorized() throws Exception {
    	// test is only if Cognito auth is working. Shell descriptor does not exist so we expect a 404 not found.
        mvc.perform(
                   MockMvcRequestBuilders
                         .get( SINGLE_SHELL_BASE_PATH, UuidCreator.getTimeOrderedEpoch() )
                         .accept( MediaType.APPLICATION_JSON )
                         .with( jwtTokenFactory.allRoles() )
             )
             .andDo( MockMvcResultHandlers.print() )
             .andExpect( status().isNotFound() );
    }
}
