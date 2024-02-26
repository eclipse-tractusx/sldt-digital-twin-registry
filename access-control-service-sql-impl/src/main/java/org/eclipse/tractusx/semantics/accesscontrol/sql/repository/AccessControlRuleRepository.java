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

import java.time.Instant;
import java.util.List;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessControlRuleRepository extends JpaRepository<AccessRule, Long> {

   @Query( """
         SELECT r
         FROM AccessRule r
         WHERE
            r.targetTenant IN (:bpn, :bpnWildcard)
            AND ( r.validFrom IS NULL OR r.validFrom <= :now )
            AND ( r.validTo IS NULL OR r.validTo >= :now )
         """ )
   List<AccessRule> findAllByBpnWithinValidityPeriod( String bpn, String bpnWildcard, Instant now );
}
