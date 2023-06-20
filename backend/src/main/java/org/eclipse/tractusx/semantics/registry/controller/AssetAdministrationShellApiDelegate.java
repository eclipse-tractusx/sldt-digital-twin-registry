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
package org.eclipse.tractusx.semantics.registry.controller;

import org.eclipse.tractusx.semantics.registry.service.ShellService;
import org.eclipse.tractusx.semantics.aas.registry.api.LookupApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.api.RegistryApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptorCollection;
import org.eclipse.tractusx.semantics.aas.registry.model.AssetAdministrationShellDescriptorCollectionBase;
import org.eclipse.tractusx.semantics.aas.registry.model.BatchResult;
import org.eclipse.tractusx.semantics.aas.registry.model.IdentifierKeyValuePair;
import org.eclipse.tractusx.semantics.aas.registry.model.ShellLookup;
import org.eclipse.tractusx.semantics.aas.registry.model.SubmodelDescriptor;
import org.eclipse.tractusx.semantics.registry.dto.BatchResultDto;
import org.eclipse.tractusx.semantics.registry.mapper.ShellMapper;
import org.eclipse.tractusx.semantics.registry.mapper.SubmodelMapper;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AssetAdministrationShellApiDelegate implements RegistryApiDelegate, LookupApiDelegate {

    private final ShellService shellService;
    private final ShellMapper shellMapper;
    private final SubmodelMapper submodelMapper;

    public AssetAdministrationShellApiDelegate(final ShellService shellService,
                                               final ShellMapper shellMapper,
                                               final SubmodelMapper submodelMapper) {
        this.shellService = shellService;
        this.shellMapper = shellMapper;
        this.submodelMapper = submodelMapper;
    }


    @Override
    public Optional<NativeWebRequest> getRequest() {
        return RegistryApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptorCollection> getAllAssetAdministrationShellDescriptors(Integer page, Integer pageSize) {
        return new ResponseEntity<>(shellMapper.toApiDto(shellService.findAllShells(page, pageSize)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptorCollectionBase> postFetchAssetAdministrationShellDescriptor(List<String> shellIdentifications) {
        List<Shell> shellsByExternalShellIds = shellService.findShellsByExternalShellIds(new HashSet<>(shellIdentifications));
        return new ResponseEntity<>(new AssetAdministrationShellDescriptorCollectionBase()
                        .items(shellMapper.toApiDto(shellsByExternalShellIds)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById(String aasIdentifier) {
        shellService.deleteShell(aasIdentifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> deleteSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
        shellService.deleteSubmodel(aasIdentifier, submodelIdentifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<SubmodelDescriptor>> getAllSubmodelDescriptors(String aasIdentifier) {
        Shell savedShell = shellService.findShellByExternalId(aasIdentifier);
        return new ResponseEntity<>(submodelMapper.toApiDto(savedShell.getSubmodels()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(String aasIdentifier) {
        Shell saved = shellService.findShellByExternalId(aasIdentifier);
        return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
        Submodel submodel = shellService.findSubmodelByExternalId(aasIdentifier, submodelIdentifier);
        return new ResponseEntity<>(submodelMapper.toApiDto(submodel), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor assetAdministrationShellDescriptor) {
        Shell saved = shellService.save(shellMapper.fromApiDto(assetAdministrationShellDescriptor));
        return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptor(String aasIdentifier, SubmodelDescriptor submodelDescriptor) {
        Submodel savedSubModel = shellService.save(aasIdentifier, submodelMapper.fromApiDto(submodelDescriptor));
        return new ResponseEntity<>(submodelMapper.toApiDto(savedSubModel), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> putAssetAdministrationShellDescriptorById(String aasIdentifier, AssetAdministrationShellDescriptor assetAdministrationShellDescriptor) {
        shellService.update(aasIdentifier, shellMapper.fromApiDto(assetAdministrationShellDescriptor)
                // the external id in the payload must not differ from the path parameter and will be overridden
                .withIdExternal(aasIdentifier));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> putSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier, SubmodelDescriptor submodelDescriptor) {
        shellService.update(aasIdentifier, submodelIdentifier, submodelMapper.fromApiDto(submodelDescriptor)
                // the external id in the payload must not differ from the path parameter and will be overridden
                .withIdExternal(submodelIdentifier));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> deleteAllAssetLinksById(String aasIdentifier) {
        shellService.deleteAllIdentifiers(aasIdentifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<String>> getAllAssetAdministrationShellIdsByAssetLink(List<IdentifierKeyValuePair> assetIds) {
        if( assetIds == null || assetIds.isEmpty()){
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        List<String> externalIds = shellService.findExternalShellIdsByIdentifiersByExactMatch(shellMapper.fromApiDto(assetIds));
        return new ResponseEntity<>(externalIds, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<IdentifierKeyValuePair>> getAllAssetLinksById(String aasIdentifier) {
        Set<ShellIdentifier> identifiers = shellService.findShellIdentifiersByExternalShellId(aasIdentifier);
        return new ResponseEntity<>(shellMapper.toApiDto(identifiers), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<IdentifierKeyValuePair>> postAllAssetLinksById(String aasIdentifier, List<IdentifierKeyValuePair> identifierKeyValuePair) {
        Set<ShellIdentifier> shellIdentifiers = shellService.save(aasIdentifier, shellMapper.fromApiDto(identifierKeyValuePair));
        return new ResponseEntity<>(shellMapper.toApiDto(shellIdentifiers), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<BatchResult>> postBatchAssetAdministrationShellDescriptor(List<AssetAdministrationShellDescriptor> assetAdministrationShellDescriptor) {
        List<Shell> shells = shellMapper.fromListApiDto(assetAdministrationShellDescriptor);
        List<BatchResultDto> batchResults = shellService.saveBatch(shells);
        return new ResponseEntity<>(shellMapper.toListApiDto(batchResults), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<String>> postQueryAllAssetAdministrationShellIds(ShellLookup shellLookup) {
        List<IdentifierKeyValuePair> assetIds = shellLookup.getQuery().getAssetIds();
        List<String> externalIds = shellService.findExternalShellIdsByIdentifiersByAnyMatch(shellMapper.fromApiDto(assetIds));
        return new ResponseEntity<>(externalIds, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> batchDeleteAssetAdministrationShellDescriptor(List<String> aasIdentifierList) {
        shellService.deleteAllShell(aasIdentifierList);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

