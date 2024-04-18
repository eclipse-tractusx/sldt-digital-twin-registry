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

import java.util.HashMap;

import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAccessControlService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Requires;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.DtrAccessVerificationClient;
import org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.DtrOauth2TokenClient;
import org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.HttpAccessVerificationClient;
import org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.Oauth2TokenClient;

@Extension( value = "DTR Data Plane HTTP Access Control Service" )
@Provides( DataPlaneAccessControlService.class )
@Requires( { HttpAccessControlCheckClientConfig.class, TypeManager.class, Vault.class, EdcHttpClient.class } )
public class DtrDataPlaneAccessControlServiceExtension implements ServiceExtension {
   @Inject
   private Monitor monitor;
   @Inject
   private EdcHttpClient httpClient;
   @Inject
   private TypeManager typeManager;
   @Inject
   private Vault vault;
   @Inject
   private HttpAccessControlCheckClientConfig config;

   @Override
   public String name() {
      return "DTR Data Plane Access Control Service";
   }

   @Provider
   public DataPlaneAccessControlService dataPlaneAccessControlService() {
      final var dtrClients = new HashMap<String, HttpAccessVerificationClient>();
      config.getDtrClientConfigMap().forEach( ( k, v ) -> {
         final Oauth2TokenClient tokenClient = new DtrOauth2TokenClient( monitor, httpClient, typeManager, vault, v );
         final HttpAccessVerificationClient client = new DtrAccessVerificationClient( monitor, httpClient, tokenClient, typeManager, config, v );
         dtrClients.put( k, client );
      } );
      final var dtrDataPlaneAccessControlService = new DtrDataPlaneAccessControlService( monitor, dtrClients, config );
      monitor.info( "Registering DtrDataPlaneAccessControlService..." );
      return dtrDataPlaneAccessControlService;
   }
}
