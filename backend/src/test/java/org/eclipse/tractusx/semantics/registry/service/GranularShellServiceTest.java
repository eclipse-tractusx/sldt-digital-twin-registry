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

package org.eclipse.tractusx.semantics.registry.service;

import java.util.Set;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;
import org.eclipse.tractusx.semantics.accesscontrol.sql.repository.AccessControlRuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( profiles = { "granular", "test" } )
@EnableConfigurationProperties( RegistryProperties.class )
class GranularShellServiceTest extends LegacyShellServiceTest {

   private static final String TENANT_ONE = "TENANT_ONE";

   @Autowired
   private AccessControlRuleRepository accessControlRuleRepository;

   @Test
   void testsLookupWithNoMatchingRecordsExpectEmptyListAndNoCursor() {
      createRule();
      super.testsLookupWithNoMatchingRecordsExpectEmptyListAndNoCursor();
   }

   @Test
   void testsLookupWithLessThanAPageOfMatchingRecordsExpectPartialListAndNoCursor() {
      createRule();
      super.testsLookupWithLessThanAPageOfMatchingRecordsExpectPartialListAndNoCursor();
   }

   @Test
   void testsLookupWithExactlyOnePageOfMatchingRecordsExpectFullListAndNoCursor() {
      createRule();
      super.testsLookupWithExactlyOnePageOfMatchingRecordsExpectFullListAndNoCursor();
   }

   @Test
   void testsLookupWithOneMoreThanOnePageOfMatchingRecordsExpectFullListAndCursor() {
      createRule();
      super.testsLookupWithOneMoreThanOnePageOfMatchingRecordsExpectFullListAndCursor();
   }

   @Test
   void testsLookupWithTwoPagesOfMatchingRecordsExpectFullListAndCursor() {
      createRule();
      super.testsLookupWithTwoPagesOfMatchingRecordsExpectFullListAndCursor();
   }

   @Test
   void testsLookupWithThreePagesOfMatchingRecordsRequestingSecondPageExpectFullListAndCursor() {
      createRule();
      super.testsLookupWithThreePagesOfMatchingRecordsRequestingSecondPageExpectFullListAndCursor();
   }

   @Test
   void testsLookupWithThreePagesOfMatchingRecordsRequestingPageOfOnlyLastItemExpectSingleItemAndNoCursor() {
      createRule();
      super.testsLookupWithThreePagesOfMatchingRecordsRequestingPageOfOnlyLastItemExpectSingleItemAndNoCursor();
   }

   private void createRule() {
      String specificAssetIdName = keyPrefix + "key";
      String specificAssetIdValue = "value";
      AccessRulePolicy policy = new AccessRulePolicy();
      policy.setAccessRules( Set.of(
            new AccessRulePolicyValue( AccessRulePolicy.BPN_RULE_NAME, PolicyOperator.EQUALS, TENANT_TWO, null ),
            new AccessRulePolicyValue( AccessRulePolicy.MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of(
                  new AccessRulePolicyValue( specificAssetIdName, PolicyOperator.EQUALS, specificAssetIdValue, null )
            ) ),
            new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of(
                  new AccessRulePolicyValue( "name", PolicyOperator.EQUALS, specificAssetIdName, null )
            ) ),
            new AccessRulePolicyValue( AccessRulePolicy.VISIBLE_SEMANTIC_IDS_RULE_NAME, PolicyOperator.INCLUDES, null, Set.of() )
      ) );
      AccessRule accessRule = new AccessRule();
      accessRule.setPolicyType( AccessRule.PolicyType.AAS );
      accessRule.setTid( TENANT_ONE );
      accessRule.setTargetTenant( TENANT_TWO );
      accessRule.setPolicy( policy );
      accessControlRuleRepository.save( accessRule );
   }
}