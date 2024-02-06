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

package org.eclipse.tractusx.semantics.accesscontrol.sql.model.converter;

import org.eclipse.tractusx.semantics.accesscontrol.sql.exception.JsonConversionException;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;

public class AccessRulePolicyConverter implements AttributeConverter<AccessRulePolicy, String> {

   private static final ObjectMapper SHARED_OBJECT_MAPPER = new ObjectMapper();

   @Override
   public String convertToDatabaseColumn( AccessRulePolicy attribute ) {
      try {
         return SHARED_OBJECT_MAPPER.writeValueAsString( attribute );
      } catch ( JsonProcessingException e ) {
         throw new JsonConversionException( e );
      }
   }

   @Override
   public AccessRulePolicy convertToEntityAttribute( String dbData ) {
      try {
         return SHARED_OBJECT_MAPPER.readValue( dbData, AccessRulePolicy.class );
      } catch ( JsonProcessingException e ) {
         throw new JsonConversionException( e );
      }
   }
}
