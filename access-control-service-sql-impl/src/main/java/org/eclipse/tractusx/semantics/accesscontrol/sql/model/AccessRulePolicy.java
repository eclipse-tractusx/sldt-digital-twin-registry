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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.policy.AccessRulePolicyValue;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnCreate;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccessRulePolicy {

   public static final String BPN_RULE_NAME = "bpn";
   public static final String MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME = "mandatorySpecificAssetIds";
   public static final String VISIBLE_SPECIFIC_ASSET_ID_NAMES_RULE_NAME = "visibleSpecificAssetIdNames";
   public static final String VISIBLE_SEMANTIC_IDS_RULE_NAME = "visibleSemanticIds";

   @Valid
   @NotNull( groups = { OnCreate.class, OnUpdate.class } )
   @Size( min = 4, groups = { OnCreate.class, OnUpdate.class } )
   @JsonProperty( "accessRules" )
   private Set<AccessRulePolicyValue> accessRules;

   private Set<SpecificAssetId> mandatorySpecificAssetIds;

   @JsonIgnore
   @Valid
   @Size( min = 1, groups = { OnCreate.class, OnUpdate.class } )
   @NotNull( groups = { OnCreate.class, OnUpdate.class } )
   public Set<SpecificAssetId> getMandatorySpecificAssetIds() {
      return accessRules.stream().filter( rule -> MANDATORY_SPECIFIC_ASSET_IDS_RULE_NAME.equals( rule.attribute() ) )
            .map( AccessRulePolicyValue::values )
            .filter( Objects::nonNull )
            .flatMap( Collection::stream )
            .map( idValue -> new SpecificAssetId( idValue.attribute(), idValue.value() ) )
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
   @NotNull( groups = { OnCreate.class, OnUpdate.class } )
   @NotBlank( groups = { OnCreate.class, OnUpdate.class } )
   public String getBpn() {
      return accessRules.stream().filter( rule -> BPN_RULE_NAME.equals( rule.attribute() ) )
            .map( this::getAccessRulePolicyValueStringFunction )
            .filter( Objects::nonNull )
            .findAny().orElse( null );
   }

   private Set<String> getStringValuesOfRule( final String ruleName ) {
      return accessRules.stream().filter( rule -> ruleName.equals( rule.attribute() ) )
            .flatMap( rule -> rule.values().stream() )
            .map( this::getAccessRulePolicyValueStringFunction )
            .collect( Collectors.toSet() );
   }

   private String getAccessRulePolicyValueStringFunction( final AccessRulePolicyValue idValue ) {
      return Optional.ofNullable( idValue ).map( AccessRulePolicyValue::value ).orElse( null );
   }
}
