/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol;

import static org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.DtrDataPlaneAccessControlConfigExtension.*;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@ExtendWith( DependencyInjectionExtension.class )
class DtrDataPlaneAccessControlConfigExtensionTest {

   @Mock
   private Monitor monitor;
   @Mock
   private Vault vault;
   @Mock
   private EdcHttpClient httpClient;

   private AutoCloseable openMocks;

   @BeforeEach
   void setUp( final ServiceExtensionContext context ) {
      openMocks = MockitoAnnotations.openMocks( this );
      doReturn( monitor ).when( context ).getMonitor();
      context.registerService( Monitor.class, monitor );
      context.registerService( EdcHttpClient.class, httpClient );
      context.registerService( Vault.class, vault );
   }

   @AfterEach
   void tearDown() {
      Assertions.assertThatNoException().isThrownBy( () -> openMocks.close() );
   }

   @Test
   void test_Initialize_ShouldSuccessfullyProcessConfiguration_WhenCalled( final DtrDataPlaneAccessControlConfigExtension extension,
         final ServiceExtensionContext context ) {
      //given
      doReturn( "default" ).when( context )
            .getSetting( eq( EDC_DTR_CONFIG_NAMES ), eq( "" ) );
      doReturn( "http://local-edc-wiremock:18080/aspect-model-api/" ).when( context )
            .getSetting( eq( EDC_DTR_CONFIG_PREFIX + "default" + ASPECT_MODEL_URL_PATTERN ), anyString() );
      doReturn( mock( Config.class ) ).when( context )
            .getConfig( EDC_DTR_CONFIG_PREFIX + "default" );

      //when
      extension.initialize( context );

      //then
      verify( context ).registerService( eq(HttpAccessControlCheckClientConfig.class), any() );
   }
}
