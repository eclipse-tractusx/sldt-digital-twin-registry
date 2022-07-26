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
package org.eclipse.tractusx.semantics.registry.security;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

import java.util.function.Predicate;

/**
    Validates the configurable tenantId claim name in a Jwt.
    The validator ensures that the Jwt contains the claim and that it's value is not blank.
 */
public class JwtTenantIdValidator  implements OAuth2TokenValidator<Jwt> {

    private final JwtClaimValidator<Object> validator;

    /**
     * @param tenantIdClaimName - The claim name of the tenant id that each {@link Jwt} should have.
     */
    public JwtTenantIdValidator(String tenantIdClaimName) {
        Assert.notNull(tenantIdClaimName, "tenantId claim name cannot be null");
        Predicate<Object> testClaimValue = (claimValue) -> {
            if(claimValue == null) {
                return false;
            }
            if( !(claimValue instanceof String) ){
                return false;
            }
            return !((String) claimValue).isBlank();
        };
        this.validator = new JwtClaimValidator<>(tenantIdClaimName, testClaimValue);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Assert.notNull(token, "token cannot be null");
        return this.validator.validate(token);
    }
}

