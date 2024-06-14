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

package org.eclipse.tractusx.semantics.accesscontrol.sql.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.tractusx.semantics.accesscontrol.api.AccessControlRuleService;
import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityContext;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityCriteria;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.repository.AccessControlRuleRepository;
import org.springframework.dao.DataAccessException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlBackedAccessControlRuleService implements AccessControlRuleService {

   private static final String NO_MATCHING_RULES_ARE_FOUND = "No matching rules are found.";
   private final AccessControlRuleRepository repository;
   private final String bpnWildcard;
   private final List<String> wildcardAllowedTypes;

   public SqlBackedAccessControlRuleService( @NonNull AccessControlRuleRepository repository, @NonNull String bpnWildcard,
         @NonNull List<String> wildcardAllowedTypes ) {
      this.repository = repository;
      this.bpnWildcard = bpnWildcard;
      this.wildcardAllowedTypes = wildcardAllowedTypes;
   }

   @Override
   public List<String> filterValidSpecificAssetIdsForLookup(
         Set<SpecificAssetId> userQuery, List<ShellVisibilityContext> shellContext, String bpn ) throws DenyAccessException {
      Set<AccessRulePolicy> allAccessControlRulesForBpn = findPotentiallyMatchingAccessControlRules( bpn ).collect( Collectors.toSet() );
      return shellContext.stream()
            .filter( aShellContext -> {
               Set<String> visibleSpecificAssetIdNamesRegardlessOfValues = allAccessControlRulesForBpn.stream()
                     .filter( accessControlRule -> aShellContext.specificAssetIds().containsAll(
                           accessControlRule.getMandatorySpecificAssetIds() ) )
                     .flatMap( this::keepVisibleSpecificAssetIdNamesWithoutValueRestrictions )
                     .collect( Collectors.toSet() );

               Map<String, Set<String>> visibleSpecificAssetIdWhenMatchingValues = allAccessControlRulesForBpn.stream()
                     .filter( accessControlRule -> aShellContext.specificAssetIds().containsAll(
                           accessControlRule.getMandatorySpecificAssetIds() ) )
                     .flatMap( this::keepVisibleSpecificAssetIdNamesWithValueRestrictions )
                     .collect( Collectors.groupingBy( SpecificAssetId::name, Collectors.mapping( SpecificAssetId::value, Collectors.toSet() ) ) );

               return aShellContext.specificAssetIds().stream()
                     .filter( id -> isSpecificAssetIdVisible( id, visibleSpecificAssetIdNamesRegardlessOfValues, visibleSpecificAssetIdWhenMatchingValues ) )
                     .collect( Collectors.toSet() )
                     .containsAll( userQuery );
            } )
            .map( ShellVisibilityContext::aasId )
            .toList();
   }

   @Override
   public ShellVisibilityCriteria fetchVisibilityCriteriaForShell( ShellVisibilityContext shellContext, String bpn ) throws DenyAccessException {
      Set<AccessRulePolicy> matchingAccessControlRules = findMatchingAccessControlRules( shellContext, bpn );
      Set<String> visibleSpecificAssetIdNamesRegardlessOfValues = matchingAccessControlRules.stream()
            .flatMap( this::keepVisibleSpecificAssetIdNamesWithoutValueRestrictions )
            .collect( Collectors.toSet() );

      Map<String, Set<String>> visibleSpecificAssetIdWhenMatchingValues = matchingAccessControlRules.stream()
            .flatMap( this::keepVisibleSpecificAssetIdNamesWithValueRestrictions )
            .collect( Collectors.groupingBy( SpecificAssetId::name, Collectors.mapping( SpecificAssetId::value, Collectors.toSet() ) ) );

      Set<String> visibleSemanticIds = matchingAccessControlRules.stream()
            .map( AccessRulePolicy::getVisibleSemanticIds )
            .flatMap( Collection::stream )
            .collect( Collectors.toSet() );
      boolean publicOnly = matchingAccessControlRules.stream().noneMatch( rule -> rule.getBpn().equals( bpn ) );
      return new ShellVisibilityCriteria( shellContext.aasId(), visibleSpecificAssetIdNamesRegardlessOfValues, visibleSpecificAssetIdWhenMatchingValues,
            visibleSemanticIds, publicOnly );
   }

   @Override
   public Map<String, ShellVisibilityCriteria> fetchVisibilityCriteriaForShells( List<ShellVisibilityContext> shellContexts, String bpn ) {
      return shellContexts.stream()
            .map( aShellContext -> {
               try {
                  return fetchVisibilityCriteriaForShell( aShellContext, bpn );
               } catch ( DenyAccessException e ) {
                  return null;
               }
            } )
            .filter( Objects::nonNull )
            .collect( Collectors.toMap( ShellVisibilityCriteria::aasId, Function.identity() ) );
   }

   private Stream<AccessRulePolicy> findPotentiallyMatchingAccessControlRules( String bpn ) throws DenyAccessException {
      try {
         List<AccessRule> allByBpn = findAllByBpn( bpn );
         if ( allByBpn == null || allByBpn.isEmpty() ) {
            throw new DenyAccessException( NO_MATCHING_RULES_ARE_FOUND );
         }
         return allByBpn.stream().map( AccessRule::getPolicy ).filter( policy -> !policy.getMandatorySpecificAssetIds().isEmpty() );
      } catch ( DataAccessException e ) {
         log.error( "Failed to fetch rules for BPN: " + bpn, e.getMessage() );
         throw new DenyAccessException( NO_MATCHING_RULES_ARE_FOUND );
      }
   }

   private List<AccessRule> findAllByBpn( String bpn ) {
      List<AccessRule> accessRules = repository.findAllByBpnWithinValidityPeriod( bpn, bpnWildcard, Instant.now() );

      accessRules.stream()
            .filter( accessRule -> bpnWildcard.equals( accessRule.getTargetTenant() ) )
            .forEach( accessRule -> {
               accessRule.getPolicy().getVisibleSpecificAssetIdNames().stream()
                     .filter( value -> !wildcardAllowedTypes.contains( value ) )
                     .forEach( value -> accessRule.getPolicy().removeVisibleSpecificAssetIdName( value ) );
            } );

      return accessRules;
   }

   private Set<AccessRulePolicy> findMatchingAccessControlRules( ShellVisibilityContext shellContext, String bpn ) throws DenyAccessException {
      Set<AccessRulePolicy> matching = findPotentiallyMatchingAccessControlRules( bpn )
            .filter( accessControlRule -> shellContext.specificAssetIds().containsAll( accessControlRule.getMandatorySpecificAssetIds() ) )
            .collect( Collectors.toSet() );
      if ( matching.isEmpty() ) {
         throw new DenyAccessException( NO_MATCHING_RULES_ARE_FOUND );
      }
      return matching;
   }

   private boolean isSpecificAssetIdVisible( SpecificAssetId specificAssetId, Set<String> visibleSpecificAssetIdNamesRegardlessOfValues,
         Map<String, Set<String>> visibleSpecificAssetIdWhenMatchingValues ) {
      return visibleSpecificAssetIdNamesRegardlessOfValues.contains( specificAssetId.name() )
            || visibleSpecificAssetIdWhenMatchingValues.getOrDefault( specificAssetId.name(), Set.of() ).contains( specificAssetId.value() );
   }

   private Stream<String> keepVisibleSpecificAssetIdNamesWithoutValueRestrictions( AccessRulePolicy accessRulePolicy ) {
      return accessRulePolicy.getVisibleSpecificAssetIdNames().stream()
            .filter( name -> accessRulePolicy.getMandatorySpecificAssetIds().stream().map( SpecificAssetId::name ).noneMatch( name::equals ) );
   }

   private Stream<SpecificAssetId> keepVisibleSpecificAssetIdNamesWithValueRestrictions( AccessRulePolicy accessRulePolicy ) {
      return accessRulePolicy.getMandatorySpecificAssetIds().stream()
            .filter( mandatory -> accessRulePolicy.getVisibleSpecificAssetIdNames().contains( mandatory.name() ) );
   }
}
