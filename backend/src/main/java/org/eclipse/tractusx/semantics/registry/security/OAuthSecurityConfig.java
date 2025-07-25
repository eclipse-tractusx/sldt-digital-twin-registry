/*******************************************************************************
 * Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.semantics.registry.security;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;

@Profile("!local")
@Configuration
@EnableWebSecurity()
public class OAuthSecurityConfig {

	@Bean(name = "jwtDecoderWithIssuerLocation")
	@ConditionalOnProperty(prefix="security.oauth2.resourceserver.jwt", name="issuer-uri")
	public JwtDecoder jwtDecoderWithIssuerLocation(@Value( "${security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
		return JwtDecoders.fromIssuerLocation(issuerUri);
	}

    /**
     * Applies the jwt token based security configuration.
     *
     * The OpenAPI generator does not support roles.
     * API Paths are authorized in this method with path and method based matchers.
     */
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
              .authorizeRequests( auth -> auth
                    .requestMatchers( HttpMethod.OPTIONS ).permitAll()

                    .requestMatchers( HttpMethod.GET, "/**/shell-descriptors" ).access( "@authorizationEvaluator.hasRoleViewDigitalTwin()" )
                    .requestMatchers( HttpMethod.GET, "/**/shell-descriptors/**" ).access( "@authorizationEvaluator.hasRoleViewDigitalTwin()" )
                    .requestMatchers( HttpMethod.GET, "/**/shell-descriptors/**/submodel-descriptors" ).access( "@authorizationEvaluator.hasRoleViewDigitalTwin()" )
                    .requestMatchers( HttpMethod.GET, "/**/shell-descriptors/**/submodel-descriptors/**" ).access( "@authorizationEvaluator.hasRoleViewDigitalTwin()" )
                    // others are HTTP method based
                    .requestMatchers( HttpMethod.POST, "/**/shell-descriptors" ).access( "@authorizationEvaluator.hasRoleAddDigitalTwin()" )
                    .requestMatchers( HttpMethod.POST, "/**/shell-descriptors/**/submodel-descriptors" ).access( "@authorizationEvaluator.hasRoleAddDigitalTwin()" )
                    .requestMatchers( HttpMethod.PUT, "/**/shell-descriptors/**" ).access( "@authorizationEvaluator.hasRoleUpdateDigitalTwin()" )
                    .requestMatchers( HttpMethod.PUT, "/**/shell-descriptors/**/submodel-descriptors/**" ).access( "@authorizationEvaluator.hasRoleUpdateDigitalTwin()" )
                    .requestMatchers( HttpMethod.DELETE, "/**/shell-descriptors/**" ).access( "@authorizationEvaluator.hasRoleDeleteDigitalTwin()" )
                    .requestMatchers( HttpMethod.DELETE, "/**/shell-descriptors/**/submodel-descriptors/**" ).access( "@authorizationEvaluator.hasRoleDeleteDigitalTwin()" )

                    // lookup
                    // query endpoint is allowed for reader
                    .requestMatchers( HttpMethod.POST, "/**/lookup/**/query/**" ).access( "@authorizationEvaluator.hasRoleViewDigitalTwin()" )
                    // others are HTTP method based
                    .requestMatchers( HttpMethod.GET, "/**/lookup/**" ).access( "@authorizationEvaluator.hasRoleViewDigitalTwin()" )
                    .requestMatchers( HttpMethod.POST, "/**/lookup/**" ).access( "@authorizationEvaluator.hasRoleAddDigitalTwin()" )
                    .requestMatchers( HttpMethod.PUT, "/**/lookup/**" ).access( "@authorizationEvaluator.hasRoleUpdateDigitalTwin()" )
                    .requestMatchers( HttpMethod.DELETE, "/**/lookup/**" ).access( "@authorizationEvaluator.hasRoleDeleteDigitalTwin()" )

                    //getDescription allowed for reader
                    .requestMatchers( HttpMethod.GET, "/**/description" ).access( "@authorizationEvaluator.hasRoleViewDigitalTwin()" )

                    //submodel access control requires special role
                    .requestMatchers( HttpMethod.POST, "/**/submodel-descriptor/authorized" ).access( "@authorizationEvaluator.hasRoleSubmodelAccessControl()" )

                    //read access rules
                    .requestMatchers( HttpMethod.GET, "/**/access-controls/rules" ).access( "@authorizationEvaluator.hasRoleReadAccessRules()" )
                    .requestMatchers( HttpMethod.GET, "/**/access-controls/rules/**" ).access( "@authorizationEvaluator.hasRoleReadAccessRules()" )

                    //write access rules
                    .requestMatchers( HttpMethod.POST, "/**/access-controls/rules" ).access( "@authorizationEvaluator.hasRoleWriteAccessRules()" )
                    .requestMatchers( HttpMethod.PUT, "/**/access-controls/rules/**" ).access( "@authorizationEvaluator.hasRoleWriteAccessRules()" )
                    .requestMatchers( HttpMethod.DELETE, "/**/access-controls/rules/**" ).access( "@authorizationEvaluator.hasRoleWriteAccessRules()" )
              )
              .csrf(CsrfConfigurer::disable)
              .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
              .oauth2ResourceServer(oauth2ResourceServerConfigurer -> oauth2ResourceServerConfigurer.jwt() );

        return http.build();
    }

    @Bean
    public AuthorizationEvaluator authorizationEvaluator(RegistryProperties registryProperties){
    	final IdentityProvider provider = IdentityProvider.getIdentityProvider(registryProperties.getIdm().getIdentityProvider());
        return provider.createAuthorizationEvaluator(registryProperties.getIdm());
    }

	/**
	 * enum containing the possible identity providers for the DTR
	 */
	public enum IdentityProvider {

		KEYCLOAK {
			@Override
			public AuthorizationEvaluator createAuthorizationEvaluator(RegistryProperties.Idm idmProperties) {
				return new KeycloakAuthorizationEvaluator(idmProperties.getPublicClientId());
			}
		},

		COGNITO {
			@Override
			public AuthorizationEvaluator createAuthorizationEvaluator(RegistryProperties.Idm idmProperties) {
				return new CognitoAuthorizationEvaluator(idmProperties);
			}
		};

		public abstract AuthorizationEvaluator createAuthorizationEvaluator(RegistryProperties.Idm idmProperties);

		/**
		 * tries to find the Identity Provider enum element based on a string, ignores
		 * case!.
		 *
		 * @param key the key to search
		 * @return the key as enum
		 */
		
		public static IdentityProvider getIdentityProvider(final String identityProviderName) {
			try {
				return IdentityProvider.valueOf(identityProviderName.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Unknown identityProvider: " + identityProviderName, e);
			}
		}
	}
}
