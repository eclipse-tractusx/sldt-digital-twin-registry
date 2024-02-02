/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
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
 *
 ******************************************************************************/
package org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyOperator {

   EQUALS( "eq" ),
   INCLUDES("includes");

   private final String value;

   PolicyOperator( String value ) {
      this.value = value;
   }

   @JsonCreator
   public static PolicyOperator forValue( final String value ) {
      return Arrays.stream( PolicyOperator.values() ).filter( o -> o.getValue().equals( value ) ).findFirst().orElse( null );
   }

   @JsonValue
   public String getValue() {
      return value;
   }
}
