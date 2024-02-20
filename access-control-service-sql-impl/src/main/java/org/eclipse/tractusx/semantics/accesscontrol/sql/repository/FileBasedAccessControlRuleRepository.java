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

package org.eclipse.tractusx.semantics.accesscontrol.sql.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class FileBasedAccessControlRuleRepository implements AccessControlRuleRepository {

   private static final TypeReference<List<AccessRule>> RULE_LIST_TYPE = new TypeReference<>() {
   };
   private final Path accessControlRulePath;
   private final ObjectMapper objectMapper;

   public FileBasedAccessControlRuleRepository(
         @Autowired ObjectMapper objectMapper,
         @Value( "${ACCESS_CONTROL_RULES_PATH:access-control-rules.json}" ) String accessControlRulePath ) {
      this.accessControlRulePath = Path.of( accessControlRulePath );
      this.objectMapper = objectMapper;
   }

   @Override
   public List<AccessRule> findAllByBpnWithinValidityPeriod( final String bpn, final String bpnWildcard ) {
      try {
         Set<String> bpns = Set.of( bpn, bpnWildcard );
         return objectMapper.readValue( accessControlRulePath.toFile(), RULE_LIST_TYPE ).stream()
               .filter( rule -> bpns.contains( rule.getTargetTenant() ) )
               .filter( rule -> {
                  Instant now = Instant.now();
                  final var validFromIsEmptyOrInThePast = Optional.ofNullable( rule.getValidFrom() )
                        .map( now::isAfter )
                        .orElse( true );
                  final var validToIsEmptyOrInTheFuture = Optional.ofNullable( rule.getValidTo() )
                        .map( now::isBefore )
                        .orElse( true );
                  return validFromIsEmptyOrInThePast && validToIsEmptyOrInTheFuture;
               } )
               .toList();
      } catch ( IOException e ) {
         throw new DataRetrievalFailureException( e.getMessage(), e );
      }
   }
}
