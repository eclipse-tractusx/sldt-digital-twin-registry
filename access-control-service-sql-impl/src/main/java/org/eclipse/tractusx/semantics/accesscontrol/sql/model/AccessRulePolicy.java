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

package org.eclipse.tractusx.semantics.accesscontrol.sql.model;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccessRulePolicy {

   private static final String BPN_RULE_NAME = "bpn";
   private static final String MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME = "mandatorySpecificAssetIds";
   private static final String VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME = "visibleSpecificAssetIdNames";
   private static final String VISIBLE_SEMANTIC_IDS_RULE_NAME = "visibleSemanticIds";

   @JsonProperty( "accessRules" )
   private Set<AccessRulePolicyValue> accessRules;

   @JsonIgnore
   public Set<SpecificAssetId> getMandatorySpecificAssetIds() {
      return accessRules.stream().filter( rule -> MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME.equals( rule.attribute() ) )
            .flatMap( rule -> {
               assertMultiValued( rule, MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME );
               return rule.values().stream();
            } )
            .map( idValue -> {
               assertSingleValued( idValue, MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME + ".*" );
               return new SpecificAssetId( idValue.attribute(), idValue.value() );
            } )
            .collect( Collectors.toSet() );
   }

   @JsonIgnore
   public Set<String> getVisibleSpecificAssetIdNames() {
      return getStringValuesOfRule( VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME );
   }

   @JsonIgnore
   public Set<String> getVisibleSemanticIds() {
      return getStringValuesOfRule( VISIBLE_SEMANTIC_IDS_RULE_NAME );
   }

   @JsonIgnore
   public String getBpn() {
      return getStringValueOfRule( BPN_RULE_NAME );
   }

   private Set<String> getStringValuesOfRule( final String ruleName ) {
      return accessRules.stream().filter( rule -> ruleName.equals( rule.attribute() ) )
            .flatMap( rule -> {
               assertMultiValued( rule, ruleName );
               return rule.values().stream();
            } )
            .map(  idValue -> getAccessRulePolicyValueStringFunction( idValue, ruleName ) )
            .collect( Collectors.toSet() );
   }

   private String getStringValueOfRule( final String ruleName ) {
      return accessRules.stream().filter( rule -> ruleName.equals( rule.attribute() ) )
            .map(  idValue -> getAccessRulePolicyValueStringFunction( idValue, ruleName ) )
            .findAny().orElse( null );
   }

   private String getAccessRulePolicyValueStringFunction( final AccessRulePolicyValue idValue, final String ruleName ) {
      assertSingleValued( idValue, ruleName );
      return idValue.value();
   }

   private void assertSingleValued( final AccessRulePolicyValue idValue, final String path ) {
      if ( !idValue.hasSingleValue() ) {
         throw new IllegalStateException( "Entry of " + path + " must have single value!" );
      }
   }

   private void assertMultiValued( AccessRulePolicyValue idValue, String path ) {
      if ( idValue.hasSingleValue() ) {
         throw new IllegalStateException( "Entry of " + path + " must have multiple values!" );
      }
   }
}
