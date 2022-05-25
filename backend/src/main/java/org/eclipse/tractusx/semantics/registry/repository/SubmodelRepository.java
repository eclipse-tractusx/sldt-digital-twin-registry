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

import org.eclipse.tractusx.semantics.registry.model.projection.SubmodelMinimal;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmodelRepository extends CrudRepository<Submodel, UUID> {

    Optional<Submodel> findByShellIdAndIdExternal(UUID shellId, String externalId);

    @Query("select s.id from submodel s where s.fk_shell_id = :shellId and s.id_external = :externalId")
    Optional<SubmodelMinimal> findMinimalRepresentationByShellIdAndIdExternal(UUID shellId, String externalId);
}
