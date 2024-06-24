/*******************************************************************************
 * Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
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

import static org.eclipse.tractusx.semantics.registry.security.AuthorizationEvaluator.Roles.*;

/**
 * This class contains methods validating JWT tokens for correctness and ensuring that the JWT token contains a desired role.
 * The methods are meant to be used in Spring Security expressions for RBAC on API operations.
 *
 * The Catena-X JWT Tokens are configured as in the example below:
 *
 *   resource_access:
 *      catenax-portal:
 *          roles:
 *              - add_digitial_twin
 *              - delete_digitial_twin
 *              - ... .. ..
 *
 * Before checking for an existing role, the token is validated first. If any attributes are not set as the expected structure,
 * the token will be considered invalid. Invalid tokens result in 403.
 *
 */
public abstract class AuthorizationEvaluator {

   private final String clientId;

   protected AuthorizationEvaluator( String clientId ) {
      this.clientId = clientId;
   }

   public boolean hasRoleViewDigitalTwin() {
      return containsRole( ROLE_VIEW_DIGITAL_TWIN );
   }

   public boolean hasRoleAddDigitalTwin() {
      return containsRole( ROLE_ADD_DIGITAL_TWIN );
   }

   public boolean hasRoleUpdateDigitalTwin() {
      return containsRole( ROLE_UPDATE_DIGITAL_TWIN );
   }

   public boolean hasRoleDeleteDigitalTwin() {
      return containsRole( ROLE_DELETE_DIGITAL_TWIN );
   }

   public boolean hasRoleSubmodelAccessControl() {
      return containsRole( ROLE_SUBMODEL_ACCESS_CONTROL );
   }

   public boolean hasRoleReadAccessRules() {
      return containsRole( ROLE_READ_ACCESS_RULES );
   }

   public boolean hasRoleWriteAccessRules() {
      return containsRole( ROLE_WRITE_ACCESS_RULES );
   }

   protected abstract boolean containsRole( String role ) ;

   /**
    * get the client id
    * @return the client id
    */
   protected String getClientId() {
	   return clientId;
   }
   
   /**
    * Represents the roles defined for the registry.
    */
   public static final class Roles {
      public static final String ROLE_VIEW_DIGITAL_TWIN = "view_digital_twin";
      public static final String ROLE_UPDATE_DIGITAL_TWIN = "update_digital_twin";
      public static final String ROLE_ADD_DIGITAL_TWIN = "add_digital_twin";
      public static final String ROLE_DELETE_DIGITAL_TWIN = "delete_digital_twin";
      public static final String ROLE_SUBMODEL_ACCESS_CONTROL = "submodel_access_control";
      public static final String ROLE_READ_ACCESS_RULES = "read_access_rules";
      public static final String ROLE_WRITE_ACCESS_RULES = "write_access_rules";
   }
}

