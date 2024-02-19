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

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

class AccessRulePolicyValidationTest extends AbstractValidationTest {

   @Test
   void testGetMandatorySpecificAssetIdsWithZeroIdsExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of(), null, null );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).hasSize( 2 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "accessRules", Set.of( "{jakarta.validation.constraints.Size.message}" ) )
               .containsEntry( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, Set.of( "{jakarta.validation.constraints.Size.message}" ) );
      }
   }

   @Test
   void testGetMandatorySpecificAssetIdsWithNullSetExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, null, null, null );
         policy.getAccessRules()
               .add( new AccessRulePolicyValue( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, null ) );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).hasSize( 4 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "accessRules", Set.of( "{jakarta.validation.constraints.Size.message}" ) )
               .containsEntry( "accessRules[]", Set.of( "Invalid rule policy." ) )
               .containsEntry( "accessRules[].values", Set.of( "Values must not be null if the policy hasSingleValue() is false." ) )
               .containsEntry( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, Set.of( "{jakarta.validation.constraints.Size.message}" ) );
      }
   }

   @Test
   void testGetMandatorySpecificAssetIdsWithMissingMandatoryIdsValuesExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, null, null, null );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).hasSize( 2 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "accessRules", Set.of( "{jakarta.validation.constraints.Size.message}" ) )
               .containsEntry( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, Set.of( "{jakarta.validation.constraints.Size.message}" ) );
      }
   }

   @Test
   void testGetBpnWithMissingValueExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = new AccessRulePolicy();
         policy.setAccessRules( Set.of(
               new AccessRulePolicyValue( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of(
                     new AccessRulePolicyValue( "name", PolicyOperator.EQUALS, "value", null )
               ) )
         ) );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).hasSize( 3 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "accessRules", Set.of( "{jakarta.validation.constraints.Size.message}" ) )
               .containsEntry( AccessRulePolicy.BPN_RULE_NAME,
                     Set.of( "{jakarta.validation.constraints.NotNull.message}", "{jakarta.validation.constraints.NotBlank.message}" ) );
      }
   }

   @Test
   void testGetBpnWithNullValueExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( null, Map.of( "name", "value" ), null, null );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).hasSize( 5 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "accessRules", Set.of( "{jakarta.validation.constraints.Size.message}" ) )
               .containsEntry( AccessRulePolicy.BPN_RULE_NAME,
                     Set.of( "{jakarta.validation.constraints.NotNull.message}", "{jakarta.validation.constraints.NotBlank.message}" ) );
      }
   }

   @Test
   void testGetVisibleSpecificAssetIdsWithNullSetExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), null, null );
         policy.getAccessRules()
               .add( new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, PolicyOperator.INCLUDES, null, null ) );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).hasSize( 3 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "accessRules", Set.of( "{jakarta.validation.constraints.Size.message}" ) )
               .containsEntry( "accessRules[]", Set.of( "Invalid rule policy." ) )
               .containsEntry( "accessRules[].values", Set.of( "Values must not be null if the policy hasSingleValue() is false." ) );
      }
   }

   @Test
   void testGetVisibleSemanticIdsWithNullSetExpectViolation() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), null, null );
         policy.getAccessRules()
               .add( new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SEMANTIC_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, null ) );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).hasSize( 3 );
         Map<String, Set<String>> violations = mapViolations( actual );
         assertThat( violations )
               .containsEntry( "accessRules", Set.of( "{jakarta.validation.constraints.Size.message}" ) )
               .containsEntry( "accessRules[]", Set.of( "Invalid rule policy." ) )
               .containsEntry( "accessRules[].values", Set.of( "Values must not be null if the policy hasSingleValue() is false." ) );
      }
   }

   @Test
   void testGetOptionalIdsWithEmptySetExpectSuccess() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), Set.of(), Set.of() );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).isEmpty();
      }
   }

   @Test
   void testFullyPopulatedWithEmptySetExpectSuccess() {
      try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
         final var underTest = factory.getValidator();
         AccessRulePolicy policy = getAccessRulePolicy( BPNA, Map.of( "name", "value" ), Set.of( "name" ), Set.of( "semanticId" ) );

         Set<ConstraintViolation<AccessRulePolicy>> actual = underTest.validate( policy, OnCreate.class );

         assertThat( actual ).isEmpty();
      }
   }

}