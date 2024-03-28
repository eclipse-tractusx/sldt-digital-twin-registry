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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAccessControlService;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@ExtendWith( DependencyInjectionExtension.class )
class DtrDataPlaneAccessControlServiceExtensionTest {

   @Mock
   private Monitor monitor;
   @Mock
   private Vault vault;
   @Mock
   private EdcHttpClient httpClient;
   @Mock
   private HttpAccessControlCheckClientConfig httpClientConfig;
   @Mock
   private HttpAccessControlCheckDtrClientConfig dtrClientConfig;

   private AutoCloseable openMocks;

   @BeforeEach
   void setUp( final ServiceExtensionContext context ) {
      openMocks = MockitoAnnotations.openMocks( this );
      context.registerService( Monitor.class, monitor );
      context.registerService( EdcHttpClient.class, httpClient );
      context.registerService( Vault.class, vault );
      context.registerService( HttpAccessControlCheckClientConfig.class, httpClientConfig );
   }

   @AfterEach
   void tearDown() {
      Assertions.assertThatNoException().isThrownBy( () -> openMocks.close() );
   }

   @Test
   void test_DataPlaneAccessControlService_ShouldBeActive_WhenClientMapIsNotEmpty( final DtrDataPlaneAccessControlServiceExtension extension,
         final ServiceExtensionContext context ) {
      //given
      when( httpClientConfig.getDtrClientConfigMap() ).thenReturn( Map.of( "default", dtrClientConfig ) );

      //when
      DataPlaneAccessControlService actual = extension.dataPlaneAccessControlService();

      //then
      assertThat( actual ).isInstanceOf( DtrDataPlaneAccessControlService.class );
      assertThat( ((DtrDataPlaneAccessControlService) actual).isNotActive() ).isFalse();
   }

   @Test
   void test_DataPlaneAccessControlService_ShouldNotBeActive_WhenClientMapIsEmpty( final DtrDataPlaneAccessControlServiceExtension extension,
         final ServiceExtensionContext context ) {
      //given
      when( httpClientConfig.getDtrClientConfigMap() ).thenReturn( Map.of() );

      //when
      DataPlaneAccessControlService actual = extension.dataPlaneAccessControlService();

      //then
      assertThat( actual ).isInstanceOf( DtrDataPlaneAccessControlService.class );
      assertThat( ((DtrDataPlaneAccessControlService) actual).isNotActive() ).isTrue();
   }
}
