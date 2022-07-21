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

import org.eclipse.tractusx.semantics.registry.JwtTokenFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestOAuthSecurityConfig {

    /**
     * In tests the OAuth2 flow is mocked by Spring. The Spring Security test support directly creates the
     * authentication object in the SecurityContextHolder.
     *
     * This decoder is only required for being present in the application context due to Spring autoconfiguration.
     */
    @Bean
    public JwtDecoder jwtDecoder(){
        return token -> {
            throw new UnsupportedOperationException("The JwtDecoder must not be called in tests by Spring.");
        };
    }

    @Bean
    public JwtTokenFactory jwtTokenFactory(RegistryProperties registryProperties){
        return new JwtTokenFactory(
                registryProperties.getIdm().getPublicClientId(),
                registryProperties.getIdm().getTenantIdClaimName()
        );
    }
}
