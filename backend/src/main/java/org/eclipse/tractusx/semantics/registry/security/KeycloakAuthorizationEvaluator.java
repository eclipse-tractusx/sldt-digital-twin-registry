/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2024 Draexlmaier Group
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

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Keycloak Authorization Evaluator
 */
public final class KeycloakAuthorizationEvaluator extends AuthorizationEvaluator {

	/**
	 * Constructor
	 * @param clientId clientId
	 */
	public KeycloakAuthorizationEvaluator(String clientId) {
		super(clientId);
	}

	   protected boolean containsRole( String role ) {
		      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		      if ( !(authentication instanceof JwtAuthenticationToken) ) {
		         return false;
		      }

		      JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) (authentication);
		      Map<String, Object> claims = jwtAuthenticationToken.getToken().getClaims();

		      Object resourceAccess = claims.get( "resource_access" );
		      if ( !(resourceAccess instanceof Map) ) {
		         return false;
		      }

		      Object resource = ((Map<String, Object>) resourceAccess).get( this.getClientId() );
		      if ( !(resource instanceof Map) ) {
		         return false;
		      }

		      Object roles = ((Map<String, Object>) resource).get( "roles" );
		      if ( !(roles instanceof Collection) ) {
		         return false;
		      }

		      Collection<String> rolesList = (Collection<String>) roles;
		      return rolesList.contains( role );
		   }
}
