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
package org.eclipse.tractusx.semantics.accesscontrol.sql.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

class AccessRuleValidationTest extends AbstractValidationTest {

   @Test
   void testCreateWithNullsExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRule rule = new AccessRule();

         Set<ConstraintViolation<AccessRule>> actual = underTest.validate( rule, OnCreate.class );

         assertThat( actual ).hasSize( 5 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "policyType", Set.of( "{jakarta.validation.constraints.NotNull.message}" ) )
               .containsEntry( "targetTenant",
                     Set.of( "{jakarta.validation.constraints.NotNull.message}", "{jakarta.validation.constraints.NotBlank.message}" ) )
               .containsEntry( "tid", Set.of( "{jakarta.validation.constraints.NotNull.message}" ) )
               .containsEntry( "policy", Set.of( "{jakarta.validation.constraints.NotNull.message}" ) );
      }
   }

   @Test
   void testCreateWithInvalidValuesExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), Set.of( "name" ), Set.of( "semanticId" ) );
         AccessRule rule = new AccessRule();
         rule.setId( 1L );
         rule.setTid( " " );
         rule.setTargetTenant( " " );
         rule.setPolicyType( AccessRule.PolicyType.AAS );
         rule.setPolicy( policy );
         rule.setDescription( "" );
         rule.setValidFrom( NOW );
         rule.setValidTo( ONE_SECOND_AGO );

         Set<ConstraintViolation<AccessRule>> actual = underTest.validate( rule, OnCreate.class );

         assertThat( actual ).hasSize( 5 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "", Set.of( "Invalid validity period." ) )
               .containsEntry( "id", Set.of( "{jakarta.validation.constraints.Null.message}" ) )
               .containsEntry( "targetTenant", Set.of( "{jakarta.validation.constraints.NotBlank.message}" ) )
               .containsEntry( "validFrom", Set.of( "ValidFrom must be earlier than validTo!" ) )
               .containsEntry( "validTo", Set.of( "ValidTo must be later than validFrom!" ) );
      }
   }

   @Test
   void testCreateWithValidValuesExpectSuccess() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), Set.of( "name" ), Set.of( "semanticId" ) );
         AccessRule rule = new AccessRule();
         rule.setTid( BPNA );
         rule.setTargetTenant( BPNA );
         rule.setPolicyType( AccessRule.PolicyType.AAS );
         rule.setPolicy( policy );
         rule.setDescription( "description" );
         rule.setValidFrom( ONE_SECOND_AGO );
         rule.setValidTo( NOW );

         Set<ConstraintViolation<AccessRule>> actual = underTest.validate( rule, OnCreate.class );

         assertThat( actual ).isEmpty();
      }
   }

   @Test
   void testUpdateWithNullsExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRule rule = new AccessRule();

         Set<ConstraintViolation<AccessRule>> actual = underTest.validate( rule, OnUpdate.class );

         assertThat( actual ).hasSize( 6 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "id", Set.of( "{jakarta.validation.constraints.NotNull.message}" ) )
               .containsEntry( "policyType", Set.of( "{jakarta.validation.constraints.NotNull.message}" ) )
               .containsEntry( "targetTenant",
                     Set.of( "{jakarta.validation.constraints.NotNull.message}", "{jakarta.validation.constraints.NotBlank.message}" ) )
               .containsEntry( "tid", Set.of( "{jakarta.validation.constraints.NotNull.message}" ) )
               .containsEntry( "policy", Set.of( "{jakarta.validation.constraints.NotNull.message}" ) );
      }
   }

   @Test
   void testUpdateWithInvalidValuesExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), Set.of( "name" ), Set.of( "semanticId" ) );
         AccessRule rule = new AccessRule();
         rule.setId( 1L );
         rule.setTid( " " );
         rule.setTargetTenant( " " );
         rule.setPolicyType( AccessRule.PolicyType.AAS );
         rule.setPolicy( policy );
         rule.setDescription( "" );
         rule.setValidFrom( NOW );
         rule.setValidTo( ONE_SECOND_AGO );

         Set<ConstraintViolation<AccessRule>> actual = underTest.validate( rule, OnUpdate.class );

         assertThat( actual ).hasSize( 4 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "", Set.of( "Invalid validity period." ) )
               .containsEntry( "targetTenant", Set.of( "{jakarta.validation.constraints.NotBlank.message}" ) )
               .containsEntry( "validFrom", Set.of( "ValidFrom must be earlier than validTo!" ) )
               .containsEntry( "validTo", Set.of( "ValidTo must be later than validFrom!" ) );
      }
   }

   @Test
   void testUpdateWithValidValuesExpectSuccess() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), Set.of( "name" ), Set.of( "semanticId" ) );
         AccessRule rule = new AccessRule();
         rule.setId( 1L );
         rule.setTid( BPNA );
         rule.setTargetTenant( BPNA );
         rule.setPolicyType( AccessRule.PolicyType.AAS );
         rule.setPolicy( policy );
         rule.setDescription( "description" );
         rule.setValidFrom( ONE_SECOND_AGO );
         rule.setValidTo( NOW );

         Set<ConstraintViolation<AccessRule>> actual = underTest.validate( rule, OnUpdate.class );

         assertThat( actual ).isEmpty();
      }
   }
}