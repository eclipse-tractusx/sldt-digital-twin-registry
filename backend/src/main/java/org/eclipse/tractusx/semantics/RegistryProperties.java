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

package org.eclipse.tractusx.semantics;

import java.util.List;

import org.eclipse.tractusx.semantics.registry.security.OAuthSecurityConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Validated
@ConfigurationProperties(prefix = "registry")
public class RegistryProperties {

    private final Idm idm = new Idm();

    /**
     * This wildcard prefix is used to make specificAssetIds public for everyone.
     * The default-value "PUBLIC_READABLE" is used by all catenaX participants.
     */
    @NotEmpty(message = "externalSubjectIdWildcardPrefix must not be empty")
    private  String externalSubjectIdWildcardPrefix;

    /**
     * This wildcard-allowed-types is used to make only specificAssetIds public for defined types.
     */
    private List<String> externalSubjectIdWildcardAllowedTypes;

    /**
     * This flag turns on the granular access control logic if set to true.
     */
    private Boolean useGranularAccessControl;
    /**
     * Configures the number of records fetched in one batch when a page of shells is requested.
     */
    private Integer granularAccessControlFetchSize;

    /**
     * Properties for Identity Management system
     */
    @Data
    @NotNull
    public static class Idm {
		/**
		 * The identityProvider that should be used. Allowed are keycloak or cognito
		 */
		@NotEmpty(message = "identityProvider must not be empty. Allowed is keycloak or cognito")
		private String identityProvider = OAuthSecurityConfig.IdentityProvider.KEYCLOAK.name().toLowerCase();

		/**
         * The public client id used for the redirect urls.
         */
        @NotEmpty(message = "public client id must not be empty")
        private String publicClientId;

		/**
		 * The internal client id used for writing operations to registry. Used if
		 * identity provider is cognito
		 */
		private String internalClientId;

		/**
         * The owning tenant id to which this AAS Registry belongs to.
         */
        @NotEmpty(message = "owningTenantId must not be empty")
        private String owningTenantId;

    }
}
