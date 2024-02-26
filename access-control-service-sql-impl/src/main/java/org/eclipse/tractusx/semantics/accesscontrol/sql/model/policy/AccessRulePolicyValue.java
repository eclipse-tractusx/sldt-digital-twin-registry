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

package org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy;

import java.util.Optional;
import java.util.Set;

import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnCreate;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnUpdate;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.ValidAccessRulePolicyValue;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;

@ValidAccessRulePolicyValue( groups = { OnCreate.class, OnUpdate.class } )
public record AccessRulePolicyValue(String attribute, PolicyOperator operator, String value, @Valid Set<AccessRulePolicyValue> values) {

   @JsonIgnore
   public boolean hasSingleValue() {
      return (operator != null && operator.isSingleValued()) || Optional.ofNullable( value ).isPresent();
   }
}
