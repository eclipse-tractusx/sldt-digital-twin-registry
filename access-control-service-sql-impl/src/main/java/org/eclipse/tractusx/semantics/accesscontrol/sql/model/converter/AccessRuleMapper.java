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

package org.eclipse.tractusx.semantics.accesscontrol.sql.model.converter;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.CreateAccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.OperatorType;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.ReadUpdateAccessRule;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

@Mapper( uses = { CustomAccessRuleMapper.class },
      componentModel = "spring",
      injectionStrategy = InjectionStrategy.CONSTRUCTOR,
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE )
public interface AccessRuleMapper {
   @Mappings( {
         @Mapping( target = "id", ignore = true ),
         @Mapping( target = "tid", ignore = true ),
         @Mapping( target = "targetTenant", ignore = true ),
         @Mapping( target = "policyType", source = "policyType" ),
         @Mapping( target = "policy", source = "policy" ),
         @Mapping( target = "description", source = "description" ),
         @Mapping( target = "validFrom",
               expression = "java( java.util.Optional.ofNullable( source.getValidFrom() ).map( java.time.OffsetDateTime::toInstant ).orElse( null ) )" ),
         @Mapping( target = "validTo",
               expression = "java( java.util.Optional.ofNullable( source.getValidTo() ).map( java.time.OffsetDateTime::toInstant ).orElse( null ) )" )
   } )
   AccessRule map( CreateAccessRule source );

   @Mappings( {
         @Mapping( target = "id", source = "id" ),
         @Mapping( target = "tid", source = "tid" ),
         @Mapping( target = "targetTenant", ignore = true ),
         @Mapping( target = "policyType", source = "policyType" ),
         @Mapping( target = "policy", source = "policy" ),
         @Mapping( target = "description", source = "description" ),
         @Mapping( target = "validFrom",
               expression = "java( java.util.Optional.ofNullable( source.getValidFrom() ).map( java.time.OffsetDateTime::toInstant ).orElse( null ) )" ),
         @Mapping( target = "validTo",
               expression = "java( java.util.Optional.ofNullable( source.getValidTo() ).map( java.time.OffsetDateTime::toInstant ).orElse( null ) )" )
   } )
   AccessRule map( ReadUpdateAccessRule source );

   @Mappings( {
         @Mapping( target = "id", source = "id" ),
         @Mapping( target = "tid", source = "tid" ),
         @Mapping( target = "policyType", source = "policyType" ),
         @Mapping( target = "policy", source = "policy" ),
         @Mapping( target = "description", source = "description" ),
         @Mapping( target = "validFrom",
               expression = "java( java.util.Optional.ofNullable( source.getValidFrom() ).map( i -> i.atOffset( java.time.ZoneOffset.UTC ) ).orElse( null ) )" ),
         @Mapping( target = "validTo",
               expression = "java( java.util.Optional.ofNullable( source.getValidTo() ).map( i -> i.atOffset( java.time.ZoneOffset.UTC ) ).orElse( null ) )" )
   } )
   ReadUpdateAccessRule map( AccessRule source );

   @ValueMappings( {
         @ValueMapping( target = "EQUALS", source = "EQ" ),
         @ValueMapping( target = "INCLUDES", source = "INCLUDES" )
   } )
   PolicyOperator map( OperatorType source );

   @ValueMappings( {
         @ValueMapping( target = "EQ", source = "EQUALS" ),
         @ValueMapping( target = "INCLUDES", source = "INCLUDES" )
   } )
   OperatorType map( PolicyOperator source );

}
