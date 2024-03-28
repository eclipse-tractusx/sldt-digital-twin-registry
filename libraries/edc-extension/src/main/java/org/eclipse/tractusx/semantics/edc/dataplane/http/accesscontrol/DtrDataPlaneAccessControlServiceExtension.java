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

import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAccessTokenService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

@Extension( value = "Data Plane HTTP Access Control" )
public class DtrDataPlaneAccessControlServiceExtension implements ServiceExtension {

   @Setting( value = "Contains the base URL of the EDC data plane endpoint where the data plane requests are sent by the end users." )
   public static final String EDC_DATA_PLANE_BASE_URL = "edc.granular.access.verification.edc.data.plane.baseUrl";
   @Setting( value = "Comma separated list of DTR configuration names used as keys for DTR clients." )
   public static final String EDC_DTR_CONFIG_NAMES = "edc.granular.access.verification.dtr.names";
   /**
    * Prefix for individual DTR configurations.
    */
   public static final String EDC_DTR_CONFIG_PREFIX = "edc.granular.access.verification.dtr.config.";
   /**
    * Configuration property suffix for the configuration of DTR decision cache. The cache is turned off if set to 0.
    */
   public static final String DTR_DECISION_CACHE_MINUTES = "dtr.decision.cache.duration.minutes";
   /**
    * Configuration property suffix for the pattern to allow for the recognition of aspect model requests which need
    * to be handled by DTR access control.
    */
   public static final String ASPECT_MODEL_URL_PATTERN = "aspect.model.url.pattern";
   /**
    * Configuration property suffix for the URL where DTR can be reached.
    */
   public static final String DTR_ACCESS_VERIFICATION_URL = "dtr.access.verification.endpoint.url";
   /**
    * Configuration property suffix for the URL where OAUTH2 tokens can be obtained for the DTR requests.
    */
   public static final String OAUTH2_TOKEN_ENDPOINT_URL = "oauth2.token.endpoint.url";
   /**
    * Configuration property suffix for the scope we need to use for OAUTH2 token requests when we need to access DTR.
    */
   public static final String OAUTH2_TOKEN_SCOPE = "oauth2.token.scope";
   /**
    * Configuration property suffix for the client id we need to use for OAUTH2 token requests when we need to access DTR.
    */
   public static final String OAUTH2_TOKEN_CLIENT_ID = "oauth2.token.clientId";

   /**
    * Configuration property suffix for the path where we can find the client secret in vault for the OAUTH2 token requests when we need to access DTR.
    */
   public static final String OAUTH2_TOKEN_CLIENT_SECRET_PATH = "oauth2.token.clientSecret.path";
   @Inject
   private Monitor monitor;
   @Inject
   private EdcHttpClient httpClient;
   @Inject
   private TypeManager typeManager;
   @Inject
   private Vault vault;
   @Inject
   private DataPlaneAccessTokenService dataPlaneAccessTokenService;
   private HttpAccessControlCheckClientConfig config;

   @Override
   public String name() {
      return "DTR Data Plane Access Control Service";
   }

   @Override
   public void initialize( final ServiceExtensionContext context ) {
      monitor.info( "Initializing " + name() );
      config = new HttpAccessControlCheckClientConfig( context );
   }
}
