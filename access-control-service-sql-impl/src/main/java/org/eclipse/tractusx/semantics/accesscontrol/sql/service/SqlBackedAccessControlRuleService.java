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

package org.eclipse.tractusx.semantics.accesscontrol.sql.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.tractusx.semantics.accesscontrol.api.AccessControlRuleService;
import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityCriteria;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRulePolicy;
import org.eclipse.tractusx.semantics.accesscontrol.sql.repository.AccessControlRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SqlBackedAccessControlRuleService implements AccessControlRuleService {

   private final AccessControlRuleRepository repository;

   public SqlBackedAccessControlRuleService( AccessControlRuleRepository repository ) {
      this.repository = repository;
   }

   @Override
   public Set<SpecificAssetId> filterValidSpecificAssetIdsForLookup( Set<SpecificAssetId> specificAssetIds, String bpn ) throws DenyAccessException {
      Set<String> visibleSpecificAssetIdNames = findMatchingAccessControlRules( specificAssetIds, bpn ).stream()
            .flatMap( accessControlRule -> Stream.concat(
                  accessControlRule.getVisibleSpecificAssetIdNames().stream(),
                  accessControlRule.getMandatorySpecificAssetIds().stream().map( SpecificAssetId::name ) ) )
            .collect( Collectors.toSet() );
      return specificAssetIds.stream()
            .filter( id -> visibleSpecificAssetIdNames.contains( id.name() ) )
            .collect( Collectors.toSet() );
   }

   @Override
   public ShellVisibilityCriteria fetchVisibilityCriteriaForShell( Set<SpecificAssetId> specificAssetIds, String bpn ) throws DenyAccessException {
      Set<AccessRulePolicy> matchingAccessControlRules = findMatchingAccessControlRules( specificAssetIds, bpn );
      Set<String> visibleSpecificAssetIdNames = matchingAccessControlRules.stream()
            .flatMap( accessControlRule -> Stream.concat(
                  accessControlRule.getVisibleSpecificAssetIdNames().stream(),
                  accessControlRule.getMandatorySpecificAssetIds().stream().map( SpecificAssetId::name ) ) )
            .collect( Collectors.toSet() );
      Set<String> visibleSemanticIds = matchingAccessControlRules.stream()
            .map( AccessRulePolicy::getVisibleSemanticIds )
            .flatMap( Collection::stream )
            .collect( Collectors.toSet() );
      return new ShellVisibilityCriteria( visibleSpecificAssetIdNames, visibleSemanticIds );
   }

   @Override
   public Set<org.eclipse.tractusx.semantics.accesscontrol.api.model.AccessRule> fetchApplicableRulesForPartner( String bpn ) throws DenyAccessException {
      return findPotentiallyMatchingAccessControlRules( bpn )
            .map( accessControlRule -> new org.eclipse.tractusx.semantics.accesscontrol.api.model.AccessRule(
                  accessControlRule.getMandatorySpecificAssetIds(), accessControlRule.getVisibleSemanticIds() ) )
            .collect( Collectors.toSet() );
   }

   private Stream<AccessRulePolicy> findPotentiallyMatchingAccessControlRules( String bpn ) throws DenyAccessException {
      List<AccessRule> allByBpn = repository.findAllByBpnWithinValidityPeriod( bpn );
      if ( allByBpn == null || allByBpn.isEmpty() ) {
         throw new DenyAccessException( "No matching rules are found." );
      }
      return allByBpn.stream().map( AccessRule::getPolicy );
   }

   private Set<AccessRulePolicy> findMatchingAccessControlRules( Set<SpecificAssetId> specificAssetIds, String bpn ) throws DenyAccessException {
      Set<AccessRulePolicy> matching = findPotentiallyMatchingAccessControlRules( bpn )
            .filter( accessControlRule -> specificAssetIds.containsAll( accessControlRule.getMandatorySpecificAssetIds() ) )
            .collect( Collectors.toSet() );
      if ( matching.isEmpty() ) {
         throw new DenyAccessException( "No matching rules are found." );
      }
      return matching;
   }

}
