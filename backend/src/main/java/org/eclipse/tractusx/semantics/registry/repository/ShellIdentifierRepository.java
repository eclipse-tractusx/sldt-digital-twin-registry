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

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface ShellIdentifierRepository extends JpaRepository<ShellIdentifier, UUID> {

   @Modifying
   @Query( value = "DELETE FROM SHELL_IDENTIFIER WHERE fk_shell_id = :shellId AND namespace != :keyToIgnore", nativeQuery = true )
   void deleteShellIdentifiersByShellId( UUID shellId, String keyToIgnore );

   Set<ShellIdentifier> findByShellId( Shell shellId );

   @Query(value = """
   SELECT s.id
   FROM shell_identifier si
   JOIN shell s ON si.fk_shell_id = s.id
   LEFT JOIN shell_identifier_external_subject_reference sies
      ON sies.FK_SHELL_IDENTIFIER_EXTERNAL_SUBJECT_ID = si.id
   LEFT JOIN shell_identifier_external_subject_reference_key sier
      ON sier.FK_SI_EXTERNAL_SUBJECT_REFERENCE_ID = sies.id
   WHERE CONCAT(si.namespace, si.identifier) IN (:keyValueCombinations)
     AND s.created_date > :cutoffDate
     AND (
         :tenantId IS NULL
         OR :tenantId = :owningTenantId
         OR sier.ref_key_value = :tenantId
         OR (
            sier.ref_key_value = :publicWildcardPrefix
            AND si.namespace IN (:publicWildcardAllowedTypes)
         )
     )
   GROUP BY s.id, s.created_date, s.id_external
   HAVING COUNT(DISTINCT CONCAT(si.namespace, si.identifier)) = :keyValueCombinationsSize
   ORDER BY s.created_date, s.id_external ASC
   """,
           nativeQuery = true,
           countQuery = """
     SELECT COUNT(*) FROM (
        SELECT s.id
        FROM shell_identifier si
        JOIN shell s ON si.fk_shell_id = s.id
        LEFT JOIN shell_identifier_external_subject_reference sies
           ON sies.FK_SHELL_IDENTIFIER_EXTERNAL_SUBJECT_ID = si.id
        LEFT JOIN shell_identifier_external_subject_reference_key sier
           ON sier.FK_SI_EXTERNAL_SUBJECT_REFERENCE_ID = sies.id
        WHERE CONCAT(si.namespace, si.identifier) IN (:keyValueCombinations)
          AND s.created_date > :cutoffDate
          AND (
             :tenantId IS NULL
             OR :tenantId = :owningTenantId
             OR sier.ref_key_value = :tenantId
             OR (
                sier.ref_key_value = :publicWildcardPrefix
                AND si.namespace IN (:publicWildcardAllowedTypes)
             )
          )
        GROUP BY s.id, s.created_date, s.id_external
        HAVING COUNT(DISTINCT CONCAT(si.namespace, si.identifier)) = :keyValueCombinationsSize
     ) AS counted
     """
   )
   Page<UUID> findAPageOfShellIdsBySpecificAssetIdsLegacyAccessControl(
           @Param("keyValueCombinations") List<String> keyValueCombinations,
           @Param("keyValueCombinationsSize") int keyValueCombinationsSize,
           @Param("cutoffDate") Instant cutoffDate,
           @Param("tenantId") String tenantId,
           @Param("owningTenantId") String owningTenantId,
           @Param("publicWildcardPrefix") String publicWildcardPrefix,
           @Param("publicWildcardAllowedTypes") List<String> publicWildcardAllowedTypes,
           Pageable pageable
   );

    @Query(value = """
              SELECT s.id
              FROM ShellIdentifier sid
                 JOIN sid.shellId s
              WHERE
                 CONCAT( sid.key, sid.value ) IN ( :keyValueCombinations )
                 AND (
                    s.createdDate > :cutoffDate
                    OR ( s.createdDate = :cutoffDate AND s.idExternal > :cursorValue )
                 )
              GROUP BY s.id, s.createdDate, s.idExternal
              HAVING COUNT(*) = :keyValueCombinationsSize
              ORDER BY s.createdDate ASC, s.idExternal ASC
            """)
    List<UUID> findAPageOfShellIdsBySpecificAssetIdsGranularAccessControl(List<String> keyValueCombinations,
            int keyValueCombinationsSize, Instant cutoffDate, String cursorValue, Pageable pageable);

   @Query( value = """
            SELECT NEW org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal(s.idExternal, sid.key, sid.value)
            FROM ShellIdentifier sid
               JOIN sid.shellId s
            WHERE
               s.id IN ( :shellIds )
            ORDER BY s.createdDate, s.idExternal ASC
         """ )
   List<ShellIdentifierMinimal> findMinimalShellIdsByShellIdsLegacyAccessControl(List<UUID> shellIds);

    @Query(value = """
               SELECT NEW org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal(s.idExternal, sid.key, sid.value)
               FROM ShellIdentifier sid
                  JOIN sid.shellId s
               WHERE
                  s.id IN ( :shellIds )
                  AND (
                     s.createdDate > :cutoffDate
                     OR ( s.createdDate = :cutoffDate AND s.idExternal > :cursorValue )
                  )
               ORDER BY s.createdDate ASC, s.idExternal ASC
            """)
    List<ShellIdentifierMinimal> findMinimalShellIdsByShellIdsGranularAccessControl(List<UUID> shellIds,
            Instant cutoffDate, String cursorValue);

   /**
    * Returns external shell ids for the given keyValueCombinations.
    * External shell ids matching the conditions below are returned:
    *   - specificAssetIds match exactly the keyValueCombinations
    *   - if externalSubjectId (tenantId) is not null it must match the tenantId
    * <p>
    * Please note that the namespace and identifier lists must be of the same size and represent key-value pairs.
    * Positional matching between these two arguments is expected, and is used to determine the pairs to search for.
    *
    * @param namespaces the lookup keys to search for
    * @param identifiers the lookup values to search for
    * @param pairCount the number of key-value pairs
    * @return external shell ids for the given key value pairs
    */
   default List<String> findExternalShellIdsByIdentifiersByExactMatch(
           @Param("namespaces") String[] namespaces,
           @Param("identifiers") String[] identifiers,
           @Param("pairCount") int pairCount,
           @Param("tenantId") String tenantId,
           @Param("publicWildcardPrefix") String publicWildcardPrefix,
           @Param("publicWildcardAllowedTypes") List<String> publicWildcardAllowedTypes,
           @Param("owningTenantId") String owningTenantId,
           @Param("globalAssetId") String globalAssetId,
           @Param("cutoffDate") Instant cutoffDate,
           @Param("cursorValue") String cursorValue,
           @Param("pageSize") int pageSize) {
       throw new UnsupportedOperationException(
               "Override provided only in the specific repositories (e.g. default, H2, PostgreSQL, ...)."
       );
   }
}
