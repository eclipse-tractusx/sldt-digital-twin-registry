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
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("default")
public interface ShellIdentifierRepositoryImpl extends ShellIdentifierRepository {

    @Override
    default List<String> findExternalShellIdsByIdentifiersByExactMatch(
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
            @Param("pageSize") int pageSize) {

        // Concatenate namespaces and identifiers for H2 IN clause
        List<String> keyValuePairs = new ArrayList<>();
        for (int i = 0; i < namespaces.size(); i++) {
            keyValuePairs.add(namespaces.get(i) + identifiers.get(i));
        }

        // Execute actual query with the converted parameters
        return findExternalShellIdsByIdentifiersByExactMatchInternal(
                keyValuePairs,
                pairCount,
                cutoffDate,
                cursorValue,
                tenantId,
                owningTenantId,
                globalAssetId,
                publicWildcardPrefix,
                publicWildcardAllowedTypes,
                pageSize);
    }

    @Query(value = """
            SELECT id_external
            FROM (
                SELECT s.id_external, s.created_date
                FROM shell s
                JOIN shell_identifier si ON s.id = si.fk_shell_id
                WHERE CONCAT(si.namespace, si.identifier) IN (:keyValuePairs)
                    AND (s.created_date > :cutoffDate OR (s.created_date = :cutoffDate AND s.id_external > :cursorValue))
                    AND :tenantId = :owningTenantId
                GROUP BY s.id_external, s.created_date
                HAVING COUNT(*) = :pairCount
            
                UNION ALL
            
                SELECT s.id_external, s.created_date
                FROM shell s
                JOIN shell_identifier si ON s.id = si.fk_shell_id
                WHERE CONCAT(si.namespace, si.identifier) IN (:keyValuePairs)
                    AND (s.created_date > :cutoffDate OR (s.created_date = :cutoffDate AND s.id_external > :cursorValue))
                    AND si.namespace = :globalAssetId
                    AND NOT (:tenantId = :owningTenantId)
                GROUP BY s.id_external, s.created_date
                HAVING COUNT(*) = :pairCount
            
                UNION ALL
            
                SELECT s.id_external, s.created_date
                FROM shell s
                JOIN shell_identifier si ON s.id = si.fk_shell_id
                WHERE CONCAT(si.namespace, si.identifier) IN (:keyValuePairs)
                    AND (s.created_date > :cutoffDate OR (s.created_date = :cutoffDate AND s.id_external > :cursorValue))
                    AND EXISTS (
                        SELECT 1
                        FROM SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE_KEY sider
                        JOIN SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE sies
                            ON sider.FK_SI_EXTERNAL_SUBJECT_REFERENCE_ID = sies.id
                        WHERE sies.FK_SHELL_IDENTIFIER_EXTERNAL_SUBJECT_ID = si.id
                            AND (sider.ref_key_value = :tenantId
                                OR (sider.ref_key_value = :publicWildcardPrefix
                                    AND si.namespace IN (:publicWildcardAllowedTypes)))
                    )
                    AND NOT (:tenantId = :owningTenantId OR si.namespace = :globalAssetId)
                GROUP BY s.id_external, s.created_date
                HAVING COUNT(*) = :pairCount
            ) AS combined_shells
            ORDER BY created_date, id_external
            LIMIT :pageSize
            """, nativeQuery = true )
    List<String> findExternalShellIdsByIdentifiersByExactMatchInternal(
            @Param("keyValuePairs") List<String> keyValuePairs,
            @Param("pairCount") int pairCount,
            @Param("cutoffDate") Instant cutoffDate,
            @Param("cursorValue") String cursorValue,
            @Param("tenantId") String tenantId,
            @Param("owningTenantId") String owningTenantId,
            @Param("globalAssetId") String globalAssetId,
            @Param("publicWildcardPrefix") String publicWildcardPrefix,
            @Param("publicWildcardAllowedTypes") List<String> publicWildcardAllowedTypes,
            @Param("pageSize") int pageSize);
}
