/*******************************************************************************
 * Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.semantics.registry.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@Profile("!test")
public interface PostgreSqlShellIdentifierRepository extends ShellIdentifierRepository {

    /**
     * Returns external shell ids for the given keyValueCombinations.
     * External shell ids matching the conditions below are returned:
     * - specificAssetIds match exactly the keyValueCombinations
     * - if externalSubjectId (tenantId) is not null it must match the tenantId
     * <p>
     * <p>
     * To be able to properly index the key and value conditions, the query does not use any functions.
     * Computed indexes cannot be created for mutable functions like CONCAT in Postgres.
     *
     * @param namespaces  the namespace values to search for, making a tuple with the identifiers
     * @param identifiers the identifier values to search for, making a tuple with the namespaces
     * @return external shell ids for the given key value combinations
     */
    @Override
    @Query(value = """
                WITH shell_lookup AS (
                SELECT s.id, s.id_external, s.created_date, si.namespace, si.identifier, si.id AS si_id
                    FROM shell s
                        JOIN shell_identifier si ON s.id = si.fk_shell_id
                        WHERE (si.namespace, si.identifier) IN (
                            SELECT unnest(:namespaces), unnest(:identifiers)
                        )
                        AND (s.created_date > :cutoffDate OR (s.created_date = :cutoffDate AND
                            s.id_external > :cursorValue))
                ) SELECT sl.id_external
                FROM shell_lookup sl
                WHERE :tenantId = :owningTenantId
                    OR namespace = :globalAssetId
                    OR EXISTS (
                        SELECT 1
                            FROM SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE_KEY sider
                            JOIN SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE sies
                                ON sider.FK_SI_EXTERNAL_SUBJECT_REFERENCE_ID = sies.id
                            WHERE
                                sies.FK_SHELL_IDENTIFIER_EXTERNAL_SUBJECT_ID = sl.si_id
                                AND (
                                    sider.ref_key_value = :tenantId
                                    OR (
                                        sider.ref_key_value = :publicWildcardPrefix
                                        AND sl.namespace IN (:publicWildcardAllowedTypes)
                                    )
                                )
                        )
                GROUP BY sl.id_external, sl.created_date
                HAVING COUNT(*) = :pairCount
                ORDER BY sl.created_date, sl.id_external
                LIMIT :pageSize;
            """, nativeQuery = true)
    List<String> findExternalShellIdsByIdentifiersByExactMatch(
            @Param("namespaces") List<String> namespaces,
            @Param("identifiers") List<String> identifiers,
            @Param("pairCount") int pairCount,
            @Param("tenantId") String tenantId,
            @Param("publicWildcardPrefix") String publicWildcardPrefix,
            @Param("publicWildcardAllowedTypes") List<String> publicWildcardAllowedTypes,
            @Param("owningTenantId") String owningTenantId,
            @Param("globalAssetId") String globalAssetId,
            @Param("cutoffDate") Instant cutoffDate,
            @Param("cursorValue") String cursorValue,
            @Param("pageSize") int pageSize);
}
