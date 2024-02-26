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

package org.eclipse.tractusx.semantics.accesscontrol.sql.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy.*;

import java.util.Set;

import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;
import org.junit.jupiter.api.Test;

class AccessRulePolicyTest {

   private static final String A = "A";
   private static final String B = "B";
   private static final String NAME = "name";
   private static final String VALUE_1 = "1";
   private static final String VALUE_2 = "2";
   private static final AccessRulePolicyValue A1 = new AccessRulePolicyValue( A, PolicyOperator.EQUALS, VALUE_1, null );
   private static final AccessRulePolicyValue B2 = new AccessRulePolicyValue( B, PolicyOperator.EQUALS, VALUE_2, null );
   private static final AccessRulePolicyValue NAME_A = new AccessRulePolicyValue( NAME, PolicyOperator.EQUALS, A, null );
   private static final AccessRulePolicyValue NAME_B = new AccessRulePolicyValue( NAME, PolicyOperator.EQUALS, B, null );
   private static final AccessRulePolicyValue BPN_A = new AccessRulePolicyValue( BPN_RULE_NAME, PolicyOperator.EQUALS, A, null );
   private static final AccessRulePolicyValue MANDATORY_SPECIFIC_ASSET_IDS_A1_B2 = new AccessRulePolicyValue(
         MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of( A1, B2 ) );
   private static final AccessRulePolicyValue VISIBLE_SPECIFIC_ASSET_ID_NAMES_A_B = new AccessRulePolicyValue(
         VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of( NAME_A, NAME_B ) );
   private static final AccessRulePolicyValue VISIBLE_SEMANTIC_IDS_A_B = new AccessRulePolicyValue(
         VISIBLE_SEMANTIC_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of( NAME_A, NAME_B ) );

   @Test
   void testGetMandatorySpecificAssetIdsExpectMap() {
      final var underTest = new AccessRulePolicy();
      underTest.setAccessRules( Set.of( MANDATORY_SPECIFIC_ASSET_IDS_A1_B2 ) );

      final var actual = underTest.getMandatorySpecificAssetIds();

      assertThat( actual ).hasSize( 2 )
            .isEqualTo( Set.of( new SpecificAssetId( A, VALUE_1 ), new SpecificAssetId( B, VALUE_2 ) ) );
   }

   @Test
   void testGetVisibleSpecificAssetIdNamesExpectSetOfNames() {
      final var underTest = new AccessRulePolicy();
      underTest.setAccessRules( Set.of( VISIBLE_SPECIFIC_ASSET_ID_NAMES_A_B ) );

      final var actual = underTest.getVisibleSpecificAssetIdNames();

      assertThat( actual ).hasSize( 2 )
            .isEqualTo( Set.of( A, B ) );
   }

   @Test
   void testGetVisibleSemanticIdsExpectSetOfNames() {
      final var underTest = new AccessRulePolicy();
      underTest.setAccessRules( Set.of( VISIBLE_SEMANTIC_IDS_A_B ) );

      final var actual = underTest.getVisibleSemanticIds();

      assertThat( actual ).hasSize( 2 )
            .isEqualTo( Set.of( A, B ) );
   }

   @Test
   void testGetBpnExpectSingleValue() {
      final var underTest = new AccessRulePolicy();
      underTest.setAccessRules( Set.of( BPN_A ) );

      final var actual = underTest.getBpn();

      assertThat( actual ).isEqualTo( A );
   }
}