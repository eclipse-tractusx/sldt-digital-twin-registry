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
import static org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.PolicyOperator;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.AasPolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.AasPolicyAccessRulesInner;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.AccessRuleValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.CreateAccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.OperatorType;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.PolicyType;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.ReadUpdateAccessRule;
import org.junit.jupiter.api.Test;

class AccessRuleMapperTest {

   private static final Long INPUT_ID = 1L;
   private static final String INPUT_BPNB = "BPNL00000000000B";
   private static final String INPUT_BPNA = "BPNL00000000000A";
   private static final Map<String, String> INPUT_MANDATORY_SPEC_ASSET_IDS = Map.of( "name1", "value1", "name2", "value2" );
   private static final Set<String> INPUT_VISIBLE_SPEC_ASSET_ID_NAMES = Set.of( "visible1", "visible2" );
   private static final Set<String> INPUT_VISIBLE_SEMANTIC_IDS = Set.of( "semanticId1", "semanticId2" );
   private static final String INPUT_DESCRIPTION = "description value";
   private static final Instant NOW = Instant.now();
   private static final Instant ONE_MINUTE_AGO = Instant.now();
   private static final OffsetDateTime NOW_DATE = NOW.atOffset( ZoneOffset.UTC );
   private static final OffsetDateTime ONE_MINUTE_AGO_DATE = ONE_MINUTE_AGO.atOffset( ZoneOffset.UTC );

   private final AccessRuleMapper underTest = new AccessRuleMapperImpl( new CustomAccessRuleMapper() );

   @Test
   void testMapCreateAccessRuleWithFullyPopulatedDataExpectSuccess() {
      final CreateAccessRule rule = new CreateAccessRule();
      rule.setPolicyType( PolicyType.AAS );
      rule.setPolicy( generatePolicy( INPUT_BPNA, INPUT_MANDATORY_SPEC_ASSET_IDS, INPUT_VISIBLE_SPEC_ASSET_ID_NAMES, INPUT_VISIBLE_SEMANTIC_IDS ) );
      rule.setDescription( INPUT_DESCRIPTION );
      rule.setValidFrom( ONE_MINUTE_AGO_DATE );
      rule.setValidTo( NOW_DATE );

      final AccessRule actual = underTest.map( rule );

      assertThat( actual )
            .isNotNull()
            .hasFieldOrPropertyWithValue( "id", null )
            .hasFieldOrPropertyWithValue( "tid", null )
            .hasFieldOrPropertyWithValue( "targetTenant", INPUT_BPNA )
            .hasFieldOrPropertyWithValue( "policyType", AccessRule.PolicyType.AAS )
            .hasFieldOrPropertyWithValue( "description", INPUT_DESCRIPTION )
            .hasFieldOrPropertyWithValue( "validFrom", ONE_MINUTE_AGO )
            .hasFieldOrPropertyWithValue( "validTo", NOW );
      assertThat( actual.getPolicy() )
            .isNotNull()
            .hasFieldOrPropertyWithValue( BPN_RULE_NAME, INPUT_BPNA )
            .hasFieldOrPropertyWithValue( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, INPUT_MANDATORY_SPEC_ASSET_IDS.entrySet().stream()
                  .map( entry -> new SpecificAssetId( entry.getKey(), entry.getValue() ) )
                  .collect( Collectors.toSet() ) )
            .hasFieldOrPropertyWithValue( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, INPUT_VISIBLE_SPEC_ASSET_ID_NAMES )
            .hasFieldOrPropertyWithValue( VISIBLE_SEMANTIC_IDS_RULE_NAME, INPUT_VISIBLE_SEMANTIC_IDS );
   }

   @Test
   void testMapCreateAccessRuleWithMinimallyPopulatedDataExpectSuccess() {
      final CreateAccessRule rule = new CreateAccessRule();
      rule.setPolicyType( PolicyType.AAS );
      rule.setPolicy( generatePolicy( INPUT_BPNA, INPUT_MANDATORY_SPEC_ASSET_IDS, Set.of(), Set.of() ) );

      final AccessRule actual = underTest.map( rule );

      assertThat( actual )
            .isNotNull()
            .hasFieldOrPropertyWithValue( "id", null )
            .hasFieldOrPropertyWithValue( "tid", null )
            .hasFieldOrPropertyWithValue( "targetTenant", INPUT_BPNA )
            .hasFieldOrPropertyWithValue( "policyType", AccessRule.PolicyType.AAS )
            .hasFieldOrPropertyWithValue( "description", null )
            .hasFieldOrPropertyWithValue( "validFrom", null )
            .hasFieldOrPropertyWithValue( "validTo", null );
      assertThat( actual.getPolicy() )
            .isNotNull()
            .hasFieldOrPropertyWithValue( BPN_RULE_NAME, INPUT_BPNA )
            .hasFieldOrPropertyWithValue( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, INPUT_MANDATORY_SPEC_ASSET_IDS.entrySet().stream()
                  .map( entry -> new SpecificAssetId( entry.getKey(), entry.getValue() ) )
                  .collect( Collectors.toSet() ) )
            .hasFieldOrPropertyWithValue( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, Set.of() )
            .hasFieldOrPropertyWithValue( VISIBLE_SEMANTIC_IDS_RULE_NAME, Set.of() );
   }

   @Test
   void testMapUpdateAccessRuleWithFullyPopulatedDataExpectSuccess() {
      final ReadUpdateAccessRule rule = new ReadUpdateAccessRule();
      rule.setId( INPUT_ID );
      rule.setTid( INPUT_BPNA );
      rule.setPolicyType( PolicyType.AAS );
      rule.setPolicy( generatePolicy( INPUT_BPNB, INPUT_MANDATORY_SPEC_ASSET_IDS, INPUT_VISIBLE_SPEC_ASSET_ID_NAMES, INPUT_VISIBLE_SEMANTIC_IDS ) );
      rule.setDescription( INPUT_DESCRIPTION );
      rule.setValidFrom( ONE_MINUTE_AGO_DATE );
      rule.setValidTo( NOW_DATE );

      final AccessRule actual = underTest.map( rule );

      assertThat( actual )
            .isNotNull()
            .hasFieldOrPropertyWithValue( "id", INPUT_ID )
            .hasFieldOrPropertyWithValue( "tid", INPUT_BPNA )
            .hasFieldOrPropertyWithValue( "targetTenant", INPUT_BPNB )
            .hasFieldOrPropertyWithValue( "policyType", AccessRule.PolicyType.AAS )
            .hasFieldOrPropertyWithValue( "description", INPUT_DESCRIPTION )
            .hasFieldOrPropertyWithValue( "validFrom", ONE_MINUTE_AGO )
            .hasFieldOrPropertyWithValue( "validTo", NOW );
      assertThat( actual.getPolicy() )
            .isNotNull()
            .hasFieldOrPropertyWithValue( BPN_RULE_NAME, INPUT_BPNB )
            .hasFieldOrPropertyWithValue( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, INPUT_MANDATORY_SPEC_ASSET_IDS.entrySet().stream()
                  .map( entry -> new SpecificAssetId( entry.getKey(), entry.getValue() ) )
                  .collect( Collectors.toSet() ) )
            .hasFieldOrPropertyWithValue( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, INPUT_VISIBLE_SPEC_ASSET_ID_NAMES )
            .hasFieldOrPropertyWithValue( VISIBLE_SEMANTIC_IDS_RULE_NAME, INPUT_VISIBLE_SEMANTIC_IDS );
   }

   @Test
   void testMapUpdateAccessRuleWithMinimallyPopulatedDataExpectSuccess() {
      final ReadUpdateAccessRule rule = new ReadUpdateAccessRule();
      rule.setId( INPUT_ID );
      rule.setTid( INPUT_BPNA );
      rule.setPolicyType( PolicyType.AAS );
      rule.setPolicy( generatePolicy( INPUT_BPNB, INPUT_MANDATORY_SPEC_ASSET_IDS, Set.of(), Set.of() ) );

      final AccessRule actual = underTest.map( rule );

      assertThat( actual )
            .isNotNull()
            .hasFieldOrPropertyWithValue( "id", INPUT_ID )
            .hasFieldOrPropertyWithValue( "tid", INPUT_BPNA )
            .hasFieldOrPropertyWithValue( "targetTenant", INPUT_BPNB )
            .hasFieldOrPropertyWithValue( "policyType", AccessRule.PolicyType.AAS )
            .hasFieldOrPropertyWithValue( "description", null )
            .hasFieldOrPropertyWithValue( "validFrom", null )
            .hasFieldOrPropertyWithValue( "validTo", null );
      assertThat( actual.getPolicy() )
            .isNotNull()
            .hasFieldOrPropertyWithValue( BPN_RULE_NAME, INPUT_BPNB )
            .hasFieldOrPropertyWithValue( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, INPUT_MANDATORY_SPEC_ASSET_IDS.entrySet().stream()
                  .map( entry -> new SpecificAssetId( entry.getKey(), entry.getValue() ) )
                  .collect( Collectors.toSet() ) )
            .hasFieldOrPropertyWithValue( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, Set.of() )
            .hasFieldOrPropertyWithValue( VISIBLE_SEMANTIC_IDS_RULE_NAME, Set.of() );
   }

   @Test
   void testMapReadAccessRuleWithFullyPopulatedDataExpectSuccess() {
      final AccessRule rule = new AccessRule();
      rule.setId( INPUT_ID );
      rule.setTid( INPUT_BPNA );
      rule.setPolicyType( AccessRule.PolicyType.AAS );
      rule.setPolicy( generatePolicyEntity( INPUT_BPNB, INPUT_MANDATORY_SPEC_ASSET_IDS, INPUT_VISIBLE_SPEC_ASSET_ID_NAMES, INPUT_VISIBLE_SEMANTIC_IDS ) );
      rule.setDescription( INPUT_DESCRIPTION );
      rule.setValidFrom( ONE_MINUTE_AGO );
      rule.setValidTo( NOW );

      final ReadUpdateAccessRule actual = underTest.map( rule );

      assertThat( actual )
            .isNotNull()
            .hasFieldOrPropertyWithValue( "id", INPUT_ID )
            .hasFieldOrPropertyWithValue( "tid", INPUT_BPNA )
            .hasFieldOrPropertyWithValue( "policyType", PolicyType.AAS )
            .hasFieldOrPropertyWithValue( "description", INPUT_DESCRIPTION )
            .hasFieldOrPropertyWithValue( "validFrom", ONE_MINUTE_AGO_DATE )
            .hasFieldOrPropertyWithValue( "validTo", NOW_DATE );
      assertThat( actual.getPolicy().getAccessRules() )
            .isNotNull()
            .hasSize( 4 )
            .contains( new AasPolicyAccessRulesInner().attribute( BPN_RULE_NAME ).operator( OperatorType.EQ ).value( INPUT_BPNB ).values( null ) )
            .contains( new AasPolicyAccessRulesInner().attribute( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME ).operator( OperatorType.INCLUDES ).values(
                  INPUT_MANDATORY_SPEC_ASSET_IDS.entrySet().stream()
                        .map( entry -> new AccessRuleValue().attribute( entry.getKey() ).operator( OperatorType.EQ ).value( entry.getValue() ) )
                        .collect( Collectors.toSet() ) ) )
            .contains( new AasPolicyAccessRulesInner().attribute( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME ).operator( OperatorType.INCLUDES ).values(
                  INPUT_VISIBLE_SPEC_ASSET_ID_NAMES.stream()
                        .map( item -> new AccessRuleValue().attribute( "name" ).operator( OperatorType.EQ ).value( item ) )
                        .collect( Collectors.toSet() ) ) )
            .contains( new AasPolicyAccessRulesInner().attribute( VISIBLE_SEMANTIC_IDS_RULE_NAME ).operator( OperatorType.INCLUDES ).values(
                  INPUT_VISIBLE_SEMANTIC_IDS.stream()
                        .map( item -> new AccessRuleValue().attribute( "modelUrn" ).operator( OperatorType.EQ ).value( item ) )
                        .collect( Collectors.toSet() ) ) );
   }

   @Test
   void testMapReadAccessRuleWithMinimallyPopulatedDataExpectSuccess() {
      final AccessRule rule = new AccessRule();
      rule.setId( INPUT_ID );
      rule.setTid( INPUT_BPNA );
      rule.setPolicyType( AccessRule.PolicyType.AAS );
      rule.setPolicy( generatePolicyEntity( INPUT_BPNB, INPUT_MANDATORY_SPEC_ASSET_IDS, Set.of(), Set.of() ) );

      final ReadUpdateAccessRule actual = underTest.map( rule );

      assertThat( actual )
            .isNotNull()
            .hasFieldOrPropertyWithValue( "id", INPUT_ID )
            .hasFieldOrPropertyWithValue( "tid", INPUT_BPNA )
            .hasFieldOrPropertyWithValue( "policyType", PolicyType.AAS )
            .hasFieldOrPropertyWithValue( "description", null )
            .hasFieldOrPropertyWithValue( "validFrom", null )
            .hasFieldOrPropertyWithValue( "validTo", null );
      assertThat( actual.getPolicy().getAccessRules() )
            .isNotNull()
            .hasSize( 4 )
            .contains( new AasPolicyAccessRulesInner().attribute( BPN_RULE_NAME ).operator( OperatorType.EQ ).value( INPUT_BPNB ).values( null ) )
            .contains( new AasPolicyAccessRulesInner().attribute( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME ).operator( OperatorType.INCLUDES ).values(
                  INPUT_MANDATORY_SPEC_ASSET_IDS.entrySet().stream()
                        .map( entry -> new AccessRuleValue().attribute( entry.getKey() ).operator( OperatorType.EQ ).value( entry.getValue() ) )
                        .collect( Collectors.toSet() ) ) )
            .contains(new AasPolicyAccessRulesInner().attribute( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME )
                  .operator( OperatorType.INCLUDES ).values( Set.of() ) )
            .contains( new AasPolicyAccessRulesInner().attribute( VISIBLE_SEMANTIC_IDS_RULE_NAME )
                  .operator( OperatorType.INCLUDES ).values( Set.of() ) );
   }

   @SuppressWarnings( "SameParameterValue" )
   private AasPolicy generatePolicy( String bpn, Map<String, String> msaId, Set<String> vsaId, Set<String> semId ) {
      return new AasPolicy()
            .addAccessRulesItem( new AasPolicyAccessRulesInner().attribute( BPN_RULE_NAME ).operator( OperatorType.EQ ).value( bpn ) )
            .addAccessRulesItem( new AasPolicyAccessRulesInner().attribute( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME ).values(
                  msaId.entrySet().stream()
                        .map( entity -> new AccessRuleValue().attribute( entity.getKey() ).operator( OperatorType.EQ ).value( entity.getValue() ) )
                        .collect( Collectors.toSet() ) ) )
            .addAccessRulesItem( new AasPolicyAccessRulesInner().attribute( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME ).values(
                  vsaId.stream()
                        .map( item -> new AccessRuleValue().attribute( "name" ).operator( OperatorType.EQ ).value( item ) )
                        .collect( Collectors.toSet() ) ) )
            .addAccessRulesItem( new AasPolicyAccessRulesInner().attribute( VISIBLE_SEMANTIC_IDS_RULE_NAME ).values(
                  semId.stream()
                        .map( item -> new AccessRuleValue().attribute( "modelUrn" ).operator( OperatorType.EQ ).value( item ) )
                        .collect( Collectors.toSet() ) ) );
   }

   @SuppressWarnings( "SameParameterValue" )
   private AccessRulePolicy generatePolicyEntity( String bpn, Map<String, String> msaId, Set<String> vsaId, Set<String> semId ) {
      AccessRulePolicy policy = new AccessRulePolicy();
      policy.setAccessRules( Set.of(
            new AccessRulePolicyValue( BPN_RULE_NAME, PolicyOperator.EQUALS, bpn, null ),
            new AccessRulePolicyValue( MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME, PolicyOperator.INCLUDES, null,
                  msaId.entrySet().stream()
                        .map( entity -> new AccessRulePolicyValue( entity.getKey(), PolicyOperator.EQUALS, entity.getValue(), null ) )
                        .collect( Collectors.toSet() ) ),
            new AccessRulePolicyValue( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME, PolicyOperator.INCLUDES, null,
                  vsaId.stream()
                        .map( item -> new AccessRulePolicyValue( "name", PolicyOperator.EQUALS, item, null ) )
                        .collect( Collectors.toSet() ) ),
            new AccessRulePolicyValue( VISIBLE_SEMANTIC_IDS_RULE_NAME, PolicyOperator.INCLUDES, null,
                  semId.stream()
                        .map( item -> new AccessRulePolicyValue( "modelUrn", PolicyOperator.EQUALS, item, null ) )
                        .collect( Collectors.toSet() ) )
      ) );
      return policy;
   }
}