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
package org.eclipse.tractusx.semantics.registry.mapper;

import com.google.common.base.Strings;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.IdentifierKeyValuePair;
import org.eclipse.tractusx.semantics.aas.registry.model.Reference;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The globalAssetId of a AssetAdministrationShellDescriptor is the same as specificAssetIds from persistence point of view.
 * This class is responsible to map the globalAssetId to {@code ShellIdentifier} and in reverse order back to the API object.
 *
 */
public class ShellMapperCustomization {

    public static Shell globalAssetIdToShellIdentifier(AssetAdministrationShellDescriptor apiDto, Shell shell){
        Optional<ShellIdentifier> shellIdentifierOptional = extractGlobalAssetId(apiDto.getGlobalAssetId());
        if(shellIdentifierOptional.isEmpty()) {
            return shell;
        }
        ShellIdentifier shellIdentifier = shellIdentifierOptional.get();
        if(shell.getIdentifiers() == null){
            return shell.withIdentifiers(Set.of(shellIdentifier));
        }
        return shell.withIdentifiers( new HashSet<>(){{
            addAll( shell.getIdentifiers());
            add(shellIdentifier);
        }});
    }

    public static void shellIdentifierToGlobalAssetId(Shell shell, AssetAdministrationShellDescriptor apiDto) {
        Optional<Reference> globalAssetId = extractGlobalAssetId(shell.getIdentifiers());
        // there are no immutable objects for the generated ones, mapping to api objects is done in mutable way
        globalAssetId.ifPresent(apiDto::setGlobalAssetId);
    }

    public static void removeGlobalAssetIdIdentifier(List<IdentifierKeyValuePair> specificAssetIds){
        if(specificAssetIds == null || specificAssetIds.isEmpty()){
            return;
        }
        specificAssetIds.removeIf(identifierKeyValuePair -> ShellIdentifier.GLOBAL_ASSET_ID_KEY.equals(identifierKeyValuePair.getKey()) );
    }

    private static Optional<Reference> extractGlobalAssetId(Set<ShellIdentifier> shellIdentifiers){
        if(shellIdentifiers == null || shellIdentifiers.isEmpty()){
            return Optional.empty();
        }
        Optional<ShellIdentifier> globalAssetId = shellIdentifiers
                .stream()
                .filter(shellIdentifier -> ShellIdentifier.GLOBAL_ASSET_ID_KEY.equals(shellIdentifier.getKey()))
                .findFirst();
        return globalAssetId.map(value -> {
            Reference reference = new Reference();
            reference.setValue(List.of(globalAssetId.get().getValue()));
            return reference;
        });
    }

    private static Optional<ShellIdentifier> extractGlobalAssetId(Reference globalAssetIdReference){
        if (globalAssetIdReference == null) {
            return Optional.empty();
        }
        List<String> value = globalAssetIdReference.getValue();
        if(value == null || value.isEmpty()){
            return Optional.empty();
        }
        String globalAssetId = value.get(0);
        if(Strings.isNullOrEmpty(globalAssetId)){
            return Optional.empty();
        }
        return Optional.of(new ShellIdentifier(null, ShellIdentifier.GLOBAL_ASSET_ID_KEY, globalAssetId, null, null));
    }
}
