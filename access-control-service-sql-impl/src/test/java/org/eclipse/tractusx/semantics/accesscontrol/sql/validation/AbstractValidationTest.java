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

package org.eclipse.tractusx.semantics.accesscontrol.sql.validation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;

import jakarta.validation.ConstraintViolation;

public abstract class AbstractValidationTest {

   protected static final String BPNA = "BPN00000A";

   protected static final Instant NOW = Instant.now();
   protected static final Instant ONE_SECOND_AGO = NOW.minus( 1, ChronoUnit.SECONDS );

   protected AccessRulePolicy getAccessRulePolicy( String bpn, Map<String, String> mandatorySaId, Set<String> visibleSaId, Set<String> semanticIds ) {
      AccessRulePolicy policy = new AccessRulePolicy();
      Set<AccessRulePolicyValue> rules = new HashSet<>();
      rules.add( new AccessRulePolicyValue( AccessRulePolicy.BPN_RULE_NAME, PolicyOperator.EQUALS, bpn, null ) );
      if ( mandatorySaId != null ) {
         rules.add( new AccessRulePolicyValue( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null,
               mandatorySaId.entrySet().stream()
                     .map( entry -> new AccessRulePolicyValue( entry.getKey(), PolicyOperator.EQUALS, entry.getValue(), null ) )
                     .collect( Collectors.toSet() )
         ) );
      }
      if ( visibleSaId != null ) {
         rules.add( new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, PolicyOperator.INCLUDES, null,
               visibleSaId.stream()
                     .map( value -> new AccessRulePolicyValue( "name", PolicyOperator.EQUALS, value, null ) )
                     .collect( Collectors.toSet() )
         ) );
      }
      if ( semanticIds != null ) {
         rules.add( new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SEMANTIC_IDS_RULE_NAME, PolicyOperator.INCLUDES, null,
               semanticIds.stream()
                     .map( value -> new AccessRulePolicyValue( "name", PolicyOperator.EQUALS, value, null ) )
                     .collect( Collectors.toSet() )
         ) );
      }
      policy.setAccessRules( rules );
      return policy;
   }

   protected <T> Map<String, Set<String>> mapViolations( Set<ConstraintViolation<T>> actual ) {
      return actual.stream()
            .collect( Collectors.groupingBy( violation -> violation.getPropertyPath().toString() ) )
            .entrySet().stream()
            .collect( Collectors.toMap( Map.Entry::getKey,
                  entry -> entry.getValue().stream().map( ConstraintViolation::getMessageTemplate ).collect( Collectors.toSet() ) ) );
   }
}
