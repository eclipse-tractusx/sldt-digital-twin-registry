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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellMinimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShellRepository extends JpaRepository<Shell, UUID>, JpaSpecificationExecutor<Shell> {
   Optional<Shell> findByIdExternal( @Param( "idExternal" ) String idExternal );

   boolean existsByIdShort( @Param( "idShort" ) String idShort );

   @Query( value = """
         SELECT *
         FROM SHELL s
         WHERE
            s.id_external = :idExternal
            AND (
               :tenantId = :owningTenantId
               OR EXISTS (
                  SELECT si.fk_shell_id
                  FROM SHELL_IDENTIFIER si
                  WHERE
                     si.fk_shell_id = s.id
                     AND EXISTS (
                        SELECT sider.ref_key_value
                        FROM SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE_KEY sider
                        WHERE
                           (
                              sider.ref_key_value = :tenantId
                              OR ( sider.ref_key_value = :publicWildcardPrefix AND si.namespace IN (:publicWildcardAllowedTypes) )
                           )
                           AND sider.FK_SI_EXTERNAL_SUBJECT_REFERENCE_ID=(
                              SELECT sies.id
                              FROM SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE sies
                              WHERE
                                 sies.FK_SHELL_IDENTIFIER_EXTERNAL_SUBJECT_ID=si.id
                           )
                     )
               )
            )
         """, nativeQuery = true )
   Optional<Shell> findByIdExternalAndExternalSubjectId( @Param( "idExternal" ) String idExternal,
         @Param( "tenantId" ) String tenantId,
         @Param( "owningTenantId" ) String owningTenantId,
         @Param( "publicWildcardPrefix" ) String publicWildcardPrefix,
         @Param( "publicWildcardAllowedTypes" ) List<String> publicWildcardAllowedTypes );

   @Query( "SELECT new org.eclipse.tractusx.semantics.registry.model.projection.ShellMinimal(s.id,s.createdDate) FROM Shell s WHERE s.idExternal = :idExternal" )
   Optional<ShellMinimal> findMinimalRepresentationByIdExternal( @Param( "idExternal" ) String idExternal );

   List<Shell> findShellsByIdExternalIsIn( Set<String> idExternals );

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
   @Query( value = """
         SELECT s.id_external
         FROM SHELL s
         WHERE
            s.id IN (
               SELECT si.fk_shell_id
               FROM SHELL_IDENTIFIER si
               WHERE
                  CONCAT(si.namespace,si.identifier) IN (:keyValueCombinations)
                  AND (
                     :tenantId = :owningTenantId
                     OR si.namespace= :globalAssetId
                     OR EXISTS (
                        SELECT sider.ref_key_value
                        FROM SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE_KEY sider
                        WHERE
                           (
                              sider.ref_key_value = :tenantId
                              OR (
                                 sider.ref_key_value = :publicWildcardPrefix
                                 AND si.namespace IN (:publicWildcardAllowedTypes)
                              )
                           )
                           AND sider.FK_SI_EXTERNAL_SUBJECT_REFERENCE_ID=(
                              SELECT sies.id
                              FROM SHELL_IDENTIFIER_EXTERNAL_SUBJECT_REFERENCE sies
                              WHERE
                                 sies.FK_SHELL_IDENTIFIER_EXTERNAL_SUBJECT_ID=si.id
                           )
                     )
                  )
               GROUP BY si.fk_shell_id
            )
         """, nativeQuery = true )
   List<String> findExternalShellIdsByIdentifiersByAnyMatch( @Param( "keyValueCombinations" ) List<String> keyValueCombinations,
         @Param( "tenantId" ) String tenantId,
         @Param( "publicWildcardPrefix" ) String publicWildcardPrefix,
         @Param( "publicWildcardAllowedTypes" ) List<String> publicWildcardAllowedTypes,
         @Param( "owningTenantId" ) String owningTenantId,
         @Param( "globalAssetId" ) String globalAssetId );

   @Query( """
         SELECT s
         FROM Shell s
         WHERE
            s.id IN (
               SELECT filterendpoint.submodel.shellId.id
               FROM SubmodelEndpoint filterendpoint
               WHERE filterendpoint.endpointAddress = :endpointAddress
            )
         """)
   List<Shell> findAllBySubmodelEndpointAddress( String endpointAddress );

   @Query("SELECT s.createdDate FROM Shell s WHERE s.idExternal = :idExternal")
   Optional<Instant> getCreatedDateByIdExternal( String idExternal );
}
