/********************************************************************************
 * Copyright (c) 2021-2023 Robert Bosch Manufacturing Solutions GmbH Copyright
 * (c) 2021-2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.semantics.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.registry.security.AuthorizationEvaluator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import lombok.Value;
import net.minidev.json.JSONArray;

public class JwtTokenFactory
{

	private static final String TENANT_ONE = "TENANT_ONE";
	private static final String TENANT_TWO = "TENANT_TWO";
	private static final String TENANT_THREE = "TENANT_THREE";

	private final Tenant tenantOne;
	private final Tenant tenantTwo;
	private final Tenant tenantThree;

	public JwtTokenFactory(final String publicClientId)
	{
		this.tenantOne = new Tenant(publicClientId, JwtTokenFactory.TENANT_ONE);
		this.tenantTwo = new Tenant(publicClientId, JwtTokenFactory.TENANT_TWO);
		this.tenantThree = new Tenant(publicClientId, JwtTokenFactory.TENANT_THREE);
	}

	public RequestPostProcessor allRoles()
	{
		return this.tenantOne.allRoles();
	}

	public RequestPostProcessor readTwin()
	{
		return this.tenantOne.readTwin();
	}

	public RequestPostProcessor addTwin()
	{
		return this.tenantOne.addTwin();
	}

	public RequestPostProcessor updateTwin()
	{
		return this.tenantOne.updateTwin();
	}

	public RequestPostProcessor deleteTwin()
	{
		return this.tenantOne.deleteTwin();
	}

	public RequestPostProcessor withoutResourceAccess()
	{
		return this.tenantOne.withoutResourceAccess();
	}

	public RequestPostProcessor withoutRoles()
	{
		return this.tenantOne.withoutRoles();
	}

	public Tenant tenantOne()
	{
		return this.tenantOne;
	}

	public Tenant tenantTwo()
	{
		return this.tenantTwo;
	}

	public Tenant tenantThree()
	{
		return this.tenantThree;
	}

	@Value
	public static class Tenant
	{
		String publicClientId;
		String tenantId;

		public RequestPostProcessor allRoles()
		{
			return this.authenticationWithRoles(this.tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_VIEW_DIGITAL_TWIN, AuthorizationEvaluator.Roles.ROLE_ADD_DIGITAL_TWIN,
					AuthorizationEvaluator.Roles.ROLE_UPDATE_DIGITAL_TWIN, AuthorizationEvaluator.Roles.ROLE_DELETE_DIGITAL_TWIN));
		}

		public RequestPostProcessor readTwin()
		{
			return this.authenticationWithRoles(this.tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_VIEW_DIGITAL_TWIN));
		}

		public RequestPostProcessor addTwin()
		{
			return this.authenticationWithRoles(this.tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_ADD_DIGITAL_TWIN));
		}

		public RequestPostProcessor updateTwin()
		{
			return this.authenticationWithRoles(this.tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_UPDATE_DIGITAL_TWIN));
		}

		public RequestPostProcessor deleteTwin()
		{
			return this.authenticationWithRoles(this.tenantId, List.of(AuthorizationEvaluator.Roles.ROLE_DELETE_DIGITAL_TWIN));
		}

		public RequestPostProcessor withoutResourceAccess()
		{
			final Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user").build();
			final Collection<GrantedAuthority> authorities = Collections.emptyList();
			return SecurityMockMvcRequestPostProcessors.authentication(new JwtAuthenticationToken(jwt, authorities));
		}

		public RequestPostProcessor withoutRoles()
		{
			final Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user")
					.claim("resource_access", Map.of(this.publicClientId, new HashMap<String, String>())).build();
			final Collection<GrantedAuthority> authorities = Collections.emptyList();
			return SecurityMockMvcRequestPostProcessors.authentication(new JwtAuthenticationToken(jwt, authorities));
		}

		private RequestPostProcessor authenticationWithRoles(final String tenantId, final List<String> roles)
		{
			final String scopes = String.join(" ", roles.stream().map(p -> "dtwinreg/" + p).collect(Collectors.toList()));

			final Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user")
					.claim("resource_access", Map.of(this.publicClientId, Map.of("roles", Tenant.toJsonArray(roles)))).claim("scope", scopes).claim("client_id", "catenax-portal")
					.build();

			final Collection<GrantedAuthority> authorities = Collections.emptyList();
			return SecurityMockMvcRequestPostProcessors.authentication(new JwtAuthenticationToken(jwt, authorities));
		}

		private static JSONArray toJsonArray(final List<String> elements)
		{
			final JSONArray jsonArray = new JSONArray();
			for(final String element : elements)
			{
				jsonArray.appendElement(element);
			}
			return jsonArray;
		}
	}

}
