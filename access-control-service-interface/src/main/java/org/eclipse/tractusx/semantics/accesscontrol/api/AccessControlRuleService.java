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

package org.eclipse.tractusx.semantics.accesscontrol.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityContext;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.ShellVisibilityCriteria;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;

public interface AccessControlRuleService {

   List<String> filterValidSpecificAssetIdsForLookup(
         Set<SpecificAssetId> userQuery, List<ShellVisibilityContext> shellContexts, String bpn ) throws DenyAccessException;

   ShellVisibilityCriteria fetchVisibilityCriteriaForShell( ShellVisibilityContext shellContext, String bpn ) throws DenyAccessException;

   Map<String, ShellVisibilityCriteria> fetchVisibilityCriteriaForShells( List<ShellVisibilityContext> shellContexts, String bpn );

}
