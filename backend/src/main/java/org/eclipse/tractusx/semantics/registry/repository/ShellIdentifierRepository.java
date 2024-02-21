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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShellIdentifierRepository extends JpaRepository<ShellIdentifier, UUID> {

   @Modifying
   @Query( value = "DELETE FROM SHELL_IDENTIFIER WHERE fk_shell_id = :shellId AND namespace != :keyToIgnore", nativeQuery = true )
   void deleteShellIdentifiersByShellId( UUID shellId, String keyToIgnore );

   Set<ShellIdentifier> findByShellId( Shell shellId );

   @Query( value = """
            SELECT NEW org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal(sid.shellId.idExternal, sid.key, sid.value)
            FROM ShellIdentifier sid
            WHERE
                 sid.shellId.id IN (
                    SELECT filtersid.shellId.id
                    FROM ShellIdentifier filtersid
                    WHERE
                        CONCAT(filtersid.key, filtersid.value) IN (:keyValueCombinations)
                    GROUP BY filtersid.shellId.id
                    HAVING COUNT(*) = :keyValueCombinationsSize
                )
         """ )
   List<ShellIdentifierMinimal> findMinimalShellIdsBySpecificAssetIds( List<String> keyValueCombinations, int keyValueCombinationsSize );

   /**
    * Returns external shell ids for the given keyValueCombinations.
    * External shell ids matching the conditions below are returned:
    *   - specificAssetIds match exactly the keyValueCombinations
    *   - if externalSubjectId (tenantId) is not null it must match the tenantId
    *
    *
    * To be able to properly index the key and value conditions, the query does not use any functions.
    * Computed indexes cannot be created for mutable functions like CONCAT in Postgres.
    *
    * @param keyValueCombinations the keys values to search for as tuples
    * @param keyValueCombinationsSize the size of the key value combinations
    * @return external shell ids for the given key value combinations
    */
   @Query( value = """
         SELECT s.id_external
         FROM shell s
            JOIN shell_identifier si ON s.id = si.fk_shell_id
         WHERE
            CONCAT(si.namespace, si.identifier) IN (:keyValueCombinations)
            AND (
               :tenantId = :owningTenantId
               OR si.namespace = :globalAssetId
               OR EXISTS (
                  SELECT 1
                  FROM SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE_KEY sider
                     JOIN SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE sies ON sider.FK_SI_EXTERNAL_SUBJECT_REFERENCE_ID = sies.id
                  WHERE
                     (
                        sider.ref_key_value = :tenantId
                        OR ( sider.ref_key_value = :publicWildcardPrefix AND si.namespace IN (:publicWildcardAllowedTypes) )
                     )
                     AND sies.FK_SHELL_IDENTIFIER_EXTERNAL_SUBJECT_ID = si.id
               )
            )
         GROUP BY s.id_external
         HAVING COUNT(*) = :keyValueCombinationsSize
         """, nativeQuery = true )
   List<String> findExternalShellIdsByIdentifiersByExactMatch( @Param( "keyValueCombinations" ) List<String> keyValueCombinations,
         @Param( "keyValueCombinationsSize" ) int keyValueCombinationsSize,
         @Param( "tenantId" ) String tenantId,
         @Param( "publicWildcardPrefix" ) String publicWildcardPrefix,
         @Param( "publicWildcardAllowedTypes" ) List<String> publicWildcardAllowedTypes,
         @Param( "owningTenantId" ) String owningTenantId,
         @Param( "globalAssetId" ) String globalAssetId );
}
