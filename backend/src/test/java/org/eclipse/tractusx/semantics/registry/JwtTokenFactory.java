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

import com.nimbusds.jose.shaded.json.JSONArray;
import lombok.Value;
import org.eclipse.tractusx.semantics.registry.security.AuthorizationEvaluator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

public class JwtTokenFactory {

    private static final String TENANT_ONE = "TENANT_ONE";
    private static final String TENANT_TWO = "TENANT_TWO";

    private final Tenant tenantOne;
    private final Tenant tenantTwo;

    public JwtTokenFactory(String publicClientId, String tenantIdClaimName){
        this.tenantOne = new Tenant(publicClientId, tenantIdClaimName, TENANT_ONE);
        this.tenantTwo = new Tenant(publicClientId, tenantIdClaimName, TENANT_TWO);
    }
    public RequestPostProcessor allRoles(){
        return tenantOne.allRoles();
    }

    public RequestPostProcessor readTwin(){
        return tenantOne.readTwin();
    }

    public RequestPostProcessor addTwin(){
        return tenantOne.addTwin();
    }

    public RequestPostProcessor updateTwin(){
        return tenantOne.updateTwin();
    }

    public RequestPostProcessor deleteTwin(){
        return tenantOne.deleteTwin();
    }

    public RequestPostProcessor withoutResourceAccess(){
        return tenantOne.withoutResourceAccess();
    }

    public RequestPostProcessor withoutRoles(){
        return tenantOne.withoutRoles();
    }

    public Tenant tenantOne() {
        return tenantOne;
    }
    public Tenant tenantTwo() {
        return tenantTwo;
    }

    @Value
    public static class Tenant{
        String publicClientId;
        String tenantIdClaimName;
        String tenantId;

        public RequestPostProcessor allRoles(){
            return authenticationWithRoles(tenantId,
                    List.of(AuthorizationEvaluator.Roles.ROLE_VIEW_DIGITAL_TWIN,
                            AuthorizationEvaluator.Roles.ROLE_ADD_DIGITAL_TWIN,
                            AuthorizationEvaluator.Roles.ROLE_UPDATE_DIGITAL_TWIN,
                            AuthorizationEvaluator.Roles.ROLE_DELETE_DIGITAL_TWIN)
            );
        }

        public RequestPostProcessor readTwin(){
            return authenticationWithRoles(tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_VIEW_DIGITAL_TWIN));
        }

        public RequestPostProcessor addTwin(){
            return authenticationWithRoles(tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_ADD_DIGITAL_TWIN));
        }

        public RequestPostProcessor updateTwin(){
            return authenticationWithRoles(tenantId,List.of(AuthorizationEvaluator.Roles.ROLE_UPDATE_DIGITAL_TWIN));
        }

        public RequestPostProcessor deleteTwin(){
            return authenticationWithRoles(tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_DELETE_DIGITAL_TWIN));
        }

        public RequestPostProcessor withoutResourceAccess(){
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", "user")
                    .build();
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            return authentication(new JwtAuthenticationToken(jwt, authorities));
        }

        public RequestPostProcessor withoutRoles(){
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", "user")
                    .claim("resource_access", Map.of(publicClientId, new HashMap<String, String>()))
                    .build();
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            return authentication(new JwtAuthenticationToken(jwt, authorities));
        }

        private RequestPostProcessor authenticationWithRoles(String tenantId, List<String> roles){
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", "user")
                    .claim("resource_access", Map.of(publicClientId, Map.of("roles", toJsonArray(roles) )))
                    .claim(tenantIdClaimName, tenantId)
                    .build();
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            return authentication(new JwtAuthenticationToken(jwt, authorities));
        }

        private static JSONArray toJsonArray(List<String> elements){
            JSONArray jsonArray = new JSONArray();
            for (String element : elements){
                jsonArray.appendElement(element);
            }
            return jsonArray;
        }
    }

}
