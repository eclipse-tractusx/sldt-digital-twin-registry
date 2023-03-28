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

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.registry.security.AuthorizationEvaluator;
import org.eclipse.tractusx.semantics.registry.security.JwtTenantIdValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Profile("!local")
@Configuration
public class OAuthSecurityConfig {

    /**
     * Applies the jwt token based security configuration.
     *
     * The OpenAPI generator does not support roles.
     * API Paths are authorized in this method with path and method based matchers.
     */
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
         http
          .authorizeRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS).permitAll()
             // fetch endpoint is allowed for reader
            .requestMatchers(HttpMethod.POST,"/**/registry/**/fetch").access("@authorizationEvaluator.hasRoleViewDigitalTwin()")
             // others are HTTP method based
            .requestMatchers(HttpMethod.GET,"/**/registry/**").access("@authorizationEvaluator.hasRoleViewDigitalTwin()")
            .requestMatchers(HttpMethod.POST,"/**/registry/**").access("@authorizationEvaluator.hasRoleAddDigitalTwin()")
            .requestMatchers(HttpMethod.PUT,"/**/registry/**").access("@authorizationEvaluator.hasRoleUpdateDigitalTwin()")
            .requestMatchers(HttpMethod.DELETE,"/**/registry/**").access("@authorizationEvaluator.hasRoleDeleteDigitalTwin()")
             // lookup
             // query endpoint is allowed for reader
            .requestMatchers(HttpMethod.POST,"/**/lookup/**/query/**").access("@authorizationEvaluator.hasRoleViewDigitalTwin()")
             // others are HTTP method based
            .requestMatchers(HttpMethod.GET,"/**/lookup/**").access("@authorizationEvaluator.hasRoleViewDigitalTwin()")
            .requestMatchers(HttpMethod.POST,"/**/lookup/**").access("@authorizationEvaluator.hasRoleAddDigitalTwin()")
            .requestMatchers(HttpMethod.PUT,"/**/lookup/**").access("@authorizationEvaluator.hasRoleUpdateDigitalTwin()")
            .requestMatchers(HttpMethod.DELETE,"/**/lookup/**").access("@authorizationEvaluator.hasRoleDeleteDigitalTwin()")
          )
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
          .oauth2ResourceServer()
          .jwt();
         return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(RegistryProperties registryProps,
                                 OAuth2ResourceServerProperties oauth2Props) {
        String issuerUri = oauth2Props.getJwt().getIssuerUri();
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> defaultIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> tenantIdValidator = new JwtTenantIdValidator(registryProps.getIdm().getTenantIdClaimName());
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultIssuer, tenantIdValidator));
        return jwtDecoder;
    }

    @Bean
    public AuthorizationEvaluator authorizationEvaluator(RegistryProperties registryProperties){
        return new AuthorizationEvaluator(registryProperties.getIdm().getPublicClientId(), registryProperties.getIdm().getTenantIdClaimName());
    }
}
