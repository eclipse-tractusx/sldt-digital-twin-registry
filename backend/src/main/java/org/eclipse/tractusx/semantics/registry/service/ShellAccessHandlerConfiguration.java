/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
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
 *
 ******************************************************************************/

package org.eclipse.tractusx.semantics.registry.service;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.accesscontrol.api.AccessControlRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShellAccessHandlerConfiguration {

    @ConditionalOnProperty(prefix = "registry", name = "use-granular-access-control", matchIfMissing = true, havingValue = "false")
    @Bean
    @Autowired
    public ShellAccessHandler createDefaultShellAccessHandler(final RegistryProperties registryProperties) {
        return new DefaultShellAccessHandler(registryProperties);
    }

    @ConditionalOnProperty(prefix = "registry", name = "use-granular-access-control", havingValue = "true")
    @Bean
    @Autowired
    public ShellAccessHandler createGranularShellAccessHandler(final RegistryProperties registryProperties, final AccessControlRuleService accessControlRuleService) {
        return new GranularShellAccessHandler(registryProperties, accessControlRuleService);
    }
}