/**
 * Copyright (c) 2024 Draexlmaier Group
 */

package org.eclipse.tractusx.semantics.registry.security;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.semantics.RegistryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Conito Autneticator We have two client_ids This is necessary because we have
 * two clients, one for reading data and one for full access
 * @author peissing
 */
public class CognitoAuthorizationEvaluator extends AuthorizationEvaluator
{
	private static final Logger log = LoggerFactory.getLogger(CognitoAuthorizationEvaluator.class);

	private final String publicClientId;
	private final String internalClientId;

	public CognitoAuthorizationEvaluator(final RegistryProperties.Idm idm)
	{
		super(idm.getPublicClientId());
		this.internalClientId = idm.getInternalClientId();
		this.publicClientId = idm.getPublicClientId();
	}

	@Override
	public boolean hasRoleViewDigitalTwin()
	{
		return this.containsCognitoRole(Roles.ROLE_VIEW_DIGITAL_TWIN);
	}

	@Override
	public boolean hasRoleAddDigitalTwin()
	{
		return this.containsCognitoRole(Roles.ROLE_ADD_DIGITAL_TWIN);
	}

	@Override
	public boolean hasRoleUpdateDigitalTwin()
	{
		return this.containsCognitoRole(Roles.ROLE_UPDATE_DIGITAL_TWIN);
	}

	@Override
	public boolean hasRoleDeleteDigitalTwin()
	{
		return this.containsCognitoRole(Roles.ROLE_DELETE_DIGITAL_TWIN);
	}

	private boolean containsCognitoRole(final String role)
	{
		CognitoAuthorizationEvaluator.log.info("Checking if token contains role {}", role);
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(!(authentication instanceof JwtAuthenticationToken))
		{
			return false;
		}

		final JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) (authentication);
		final Map<String, Object> claims = jwtAuthenticationToken.getToken().getClaims();

		final Object claimClientId = claims.get("client_id");

		if(StringUtils.equals(this.internalClientId, (String) claimClientId) || StringUtils.equals(this.publicClientId, (String) claimClientId))
		{
			final Object scope = claims.get("scope");
			if(scope instanceof final String scopeString)
			{
				final String[] split = StringUtils.split(scopeString, ' ');
				for(final String tokenRole : split)
				{
					if(StringUtils.contains(tokenRole, role))
					{
						CognitoAuthorizationEvaluator.log.info("Role {} found in token", role);
						return true;
					}
				}
			}
		}

		final Object roles = claims.get("cognito:groups");
		if(roles instanceof final Collection<?> rolesList)
		{
			return rolesList.contains(role);
		}
		return false;
	}
}
