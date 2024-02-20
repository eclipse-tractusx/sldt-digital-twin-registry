/*******************************************************************************
 * Copyright (c) 2021 T-Systems International GmbH and others
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

package org.eclipse.tractusx.semantics;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main Adapter Application
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(RegistryProperties.class)
@ComponentScan(basePackages = {"org.eclipse.tractusx.semantics", "org.openapitools.configuration"})
public class RegistryApplication {

	private static final String OPEN_ID_CONNECT_DISCOVERY_PATH = "/.well-known/openid-configuration";

	@Bean
	public WebMvcConfigurer configurer(OAuth2ResourceServerProperties securityProperties) {
		return new WebMvcConfigurer(){
			@Override
			public void addCorsMappings(CorsRegistry registry) {
			    registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
			}
			@Override
			public void addViewControllers(ViewControllerRegistry registry){
				// this redirect ensures that the SwaggerUI can get the open id discovery data
				String fullDiscoveryPath = securityProperties.getJwt().getIssuerUri() + OPEN_ID_CONNECT_DISCOVERY_PATH;
				registry.addRedirectViewController(OPEN_ID_CONNECT_DISCOVERY_PATH, fullDiscoveryPath);
			}
		};
	}

	@Bean
	public SpringDocConfiguration springDocConfiguration(){
		return new SpringDocConfiguration();
	}

	@Bean
	public SpringDocConfigProperties springDocConfigProperties() {
		return new SpringDocConfigProperties();
	}

	@Bean
	ObjectMapperProvider objectMapperProvider(SpringDocConfigProperties springDocConfigProperties){
		return new ObjectMapperProvider( springDocConfigProperties );
	}

	@Bean
	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedSlash(true);
		return firewall;
	}

	/**
	 * entry point if started as an app
	 * @param args command line
	 */
	public static void main(String[] args) {
		new SpringApplication(RegistryApplication.class).run(args);
	}

}
