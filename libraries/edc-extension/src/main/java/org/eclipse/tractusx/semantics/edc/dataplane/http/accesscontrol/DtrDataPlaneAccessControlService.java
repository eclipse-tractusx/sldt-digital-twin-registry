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

import static org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.DtrAccessVerificationClient.HEADER_EDC_BPN;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAccessControlService;
import org.eclipse.edc.spi.iam.ClaimToken;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.DtrAccessVerificationClient;
import org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.HttpAccessVerificationClient;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;

public class DtrDataPlaneAccessControlService implements DataPlaneAccessControlService {
   static final String ADDRESS_HEADER_EDC_BPN = "header:" + DtrAccessVerificationClient.HEADER_EDC_BPN;
   static final String ADDRESS_ASSET_BASE_URL = "https://w3id.org/edc/v0.0.1/ns/baseUrl";

   private final Monitor monitor;

   private final HttpAccessControlCheckClientConfig config;

   private final Map<String, HttpAccessVerificationClient> clients;

   public DtrDataPlaneAccessControlService(
         final Monitor monitor,
         final Map<String, HttpAccessVerificationClient> clients,
         final HttpAccessControlCheckClientConfig config ) {
      this.monitor = monitor;
      this.clients = clients;
      this.config = config;
   }

   @Override
   public Result<Void> checkAccess( ClaimToken claimToken, DataAddress address, Map<String, Object> requestData, Map<String, Object> additionalData ) {
      if ( isNotActive() ) {
         //no DTR clients are registered, therefore the extension is turned off
         return Result.success();
      }
      if ( !requestData.containsKey( "path" ) || !(requestData.get( "path" ) instanceof UriRoutingContext uriRoutingContext) ) {
         monitor.warning( "Skipping access control check as requestData.path does not contain uriRoutingContext: "
                          + requestData.getOrDefault( "path", null ) );
         return Result.success();
      }
      final var requestUri = uriRoutingContext.getRequestUri();
      final var path = requestUri.getPath().replaceFirst( "^/public/v2/", "" );
      final var queryString = requestUri.getQuery();
      final Optional<String> baseUrlNoTrailingSlash = Optional.ofNullable( address.getStringProperty( ADDRESS_ASSET_BASE_URL ) )
            .map( url -> url.replaceFirst( "/$", "" ) );
      if ( baseUrlNoTrailingSlash.isEmpty() ) {
         monitor.severe( "Failed to obtain base URL from address!" );
         return Result.failure( "" );
      }
      final var targetUrl = baseUrlNoTrailingSlash.get() + "/" + path;
      final Map<String, String> additionalHeaders = Optional.ofNullable( address.getStringProperty( ADDRESS_HEADER_EDC_BPN ) )
            .map( consumerBpn -> Map.of( HEADER_EDC_BPN, consumerBpn ) )
            .orElse( Collections.emptyMap() );
      final var relevantClients = clients.values().stream()
            .filter( client -> client.isAspectModelCall( targetUrl ) )
            .collect( Collectors.toSet() );
      if ( !relevantClients.isEmpty() && relevantClients.stream()
            .noneMatch( client -> client.shouldAllowAccess( path, queryString, additionalHeaders ) ) ) {
         return Result.failure( "Forbidden." );
      }

      return Result.success();
   }

   protected boolean isNotActive() {
      return config.getDtrClientConfigMap().isEmpty();
   }
}
