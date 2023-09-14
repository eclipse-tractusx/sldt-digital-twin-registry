/********************************************************************************
 * Copyright (c) 2021-2022 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2021-2022 Contributors to the Eclipse Foundation
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
 ********************************************************************************/
package org.eclipse.tractusx.semantics.registry.repository;

import org.eclipse.tractusx.semantics.registry.model.projection.ShellMinimal;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ShellRepository extends PagingAndSortingRepository<Shell, UUID>, CrudRepository<Shell,UUID> {
    Optional<Shell> findByIdExternal(String idExternal);

    @Query("select s.id, s.created_date, s.tenant_id from shell s where s.id_external = :idExternal")
    Optional<ShellMinimal> findMinimalRepresentationByIdExternal(String idExternal);

    List<Shell> findShellsByIdExternalIsIn(Set<String> idExternals);

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
    @Query(
            "select s.id_external from shell s join shell_identifier si on s.id = si.fk_shell_id " +
                    "where concat(si.namespace,si.identifier) in (:keyValueCombinations) " +
                    "AND ((si.external_subject_id is not null and si.external_subject_id = :tenantId) " +
                    "or :tenantId = s.tenant_id) " +
                    "group by s.id_external having count(*) = :keyValueCombinationsSize"
    )
    List<String> findExternalShellIdsByIdentifiersByExactMatch(@Param("keyValueCombinations") List<String>  keyValueCombinations,
                                                   @Param("keyValueCombinationsSize") int keyValueCombinationsSize,
                                                   @Param("tenantId") String tenantId);

    /**
     * Returns external shell ids for the given keyValueCombinations.
     * External shell ids that match any keyValueCombinations are returned.
     *
     * To be able to properly index the key and value conditions, the query does not use any functions.
     * Computed indexes cannot be created for mutable functions like CONCAT in Postgres.
     *
     * @param keyValueCombinations the keys values to search for as tuples
     * @return external shell ids for the given key value combinations
     */
    @Query(
            "select distinct s.id_external from shell s where s.id in (" +
                    "select si.fk_shell_id from shell_identifier si " +
                    "where concat(si.namespace,si.identifier) in (:keyValueCombinations) " +
                    "AND ((si.external_subject_id is not null and si.external_subject_id = :tenantId) or :tenantId = s.tenant_id) " +
                    "group by si.fk_shell_id " +
                    ")"
    )
    List<String> findExternalShellIdsByIdentifiersByAnyMatch(@Param("keyValueCombinations") List<String> keyValueCombinations,
                                                             @Param("tenantId") String tenantId);
}
