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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;
import org.junit.jupiter.api.Test;

class AccessRulePolicyConverterTest {
   private static final AccessRulePolicyValue A_EQ_1_RULE_VALUE = new AccessRulePolicyValue( "A", PolicyOperator.EQUALS, "1", null );
   private static final AccessRulePolicyValue B_EQ_2_RULE_VALUE = new AccessRulePolicyValue( "B", PolicyOperator.EQUALS, "2", null );
   private static final AccessRulePolicyValue CA_EQ_1_RULE_VALUE = new AccessRulePolicyValue( "CA", PolicyOperator.EQUALS, "1", null );
   private static final AccessRulePolicyValue CB_EQ_2_RULE_VALUE = new AccessRulePolicyValue( "CB", PolicyOperator.EQUALS, "2", null );
   private static final AccessRulePolicyValue C_INCLUDES_CA1_AND_CB2_RULE_VALUE = new AccessRulePolicyValue( "C", PolicyOperator.INCLUDES, null,
         Set.of( CA_EQ_1_RULE_VALUE, CB_EQ_2_RULE_VALUE ) );

   @Test
   void testConvertToEntityAttributeWithTheResultOfConvertToDatabaseColumnExpectOriginalObject() {
      final var policy = new AccessRulePolicy();
      policy.setAccessRules( Set.of( A_EQ_1_RULE_VALUE, B_EQ_2_RULE_VALUE, C_INCLUDES_CA1_AND_CB2_RULE_VALUE ) );
      final var underTest = new AccessRulePolicyConverter();

      final var columnValue = underTest.convertToDatabaseColumn( policy );
      final var actual = underTest.convertToEntityAttribute( columnValue );

      assertThat(actual).isEqualTo( policy );
   }
}