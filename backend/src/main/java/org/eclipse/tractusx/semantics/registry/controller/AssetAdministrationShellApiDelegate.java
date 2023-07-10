/********************************************************************************
 * Copyright (c) 2021-2023 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2021-2023 Contributors to the Eclipse Foundation
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

import java.util.*;

import org.eclipse.tractusx.semantics.aas.registry.api.DescriptionApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.api.LookupApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.api.ShellDescriptorsApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.model.*;
import org.eclipse.tractusx.semantics.registry.dto.ShellCollectionDto;
import org.eclipse.tractusx.semantics.registry.mapper.ShellMapper;
import org.eclipse.tractusx.semantics.registry.mapper.SubmodelMapper;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.service.ShellService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

@Service
public class AssetAdministrationShellApiDelegate implements DescriptionApiDelegate, ShellDescriptorsApiDelegate, LookupApiDelegate {

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
        return DescriptionApiDelegate.super.getRequest();
    }

    @Override
    // TODO: 21.06.2023 implement correct and not just give back dummy, implement test cases for this endpoint 
    public ResponseEntity<ServiceDescription> getDescription() {
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setProfiles( List.of( ServiceDescription.ProfilesEnum.ASSETADMINISTRATIONSHELLREPOSITORYSERVICESPECIFICATION_V3_0_MINIMALPROFILE, 
              ServiceDescription.ProfilesEnum.REGISTRYSERVICESPECIFICATION_V3_0) );
        return  new ResponseEntity<>( serviceDescription, HttpStatus.OK );
    }

    @Override
    public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById( String  aasIdentifier ) {
        shellService.deleteShell( aasIdentifier );
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @Override
    public ResponseEntity<Void> deleteAllAssetLinksById(byte[] aasIdentifier) {

        shellService.deleteAllIdentifiers(getDecodedId(aasIdentifier));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @Override
    public ResponseEntity<Void> deleteSubmodelDescriptorByIdThroughSuperpath( byte[] aasIdentifier, byte[] submodelIdentifier ) {
        shellService.deleteSubmodel(getDecodedId( aasIdentifier ), getDecodedId( submodelIdentifier ));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @Override
    public ResponseEntity<GetAssetAdministrationShellDescriptorsResult> getAllAssetAdministrationShellDescriptors( Integer limit, String cursor,
          AssetKind assetKind, String assetType ) {
        Integer page = 0 ;
        Integer pageSize = 100;
        ShellCollectionDto dto =  shellService.findAllShells(page, pageSize);
        GetAssetAdministrationShellDescriptorsResult result = shellMapper.toApiDto(dto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    // new todo: correct implementation
    public ResponseEntity<GetSubmodelDescriptorsResult> getAllSubmodelDescriptorsThroughSuperpath( byte[] aasIdentifier, Integer limit, String cursor ) {
        Shell savedShell = shellService.findShellByExternalId(getDecodedId( aasIdentifier ));
        Set<Submodel> submodels = savedShell.getSubmodels();
        List<SubmodelDescriptor> descriptorResults = submodelMapper.toApiDto( submodels );
        GetSubmodelDescriptorsResult result = new GetSubmodelDescriptorsResult();
        result.setResult( descriptorResults );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById( byte[] aasIdentifier ) {
        String decodedAasIdentifier = getDecodedId( aasIdentifier );
        Shell saved = shellService.findShellByExternalId(decodedAasIdentifier);
        return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.OK);
    }

    private static String getDecodedId( byte[] aasIdentifier ) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode( aasIdentifier );
        String decodedAasIdentifier = new String(decodedBytes);
        return decodedAasIdentifier;
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorByIdThroughSuperpath( byte[] aasIdentifier, byte[] submodelIdentifier ) {
        Submodel submodel = shellService.findSubmodelByExternalId(getDecodedId( aasIdentifier ), getDecodedId( submodelIdentifier ));
        return new ResponseEntity<>(submodelMapper.toApiDto(submodel), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor( AssetAdministrationShellDescriptor assetAdministrationShellDescriptor ) {
        Shell saved = shellService.save(shellMapper.fromApiDto(assetAdministrationShellDescriptor));
        return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptorThroughSuperpath( byte[] aasIdentifier, SubmodelDescriptor submodelDescriptor ) {
        Submodel toBeSaved = submodelMapper.fromApiDto(submodelDescriptor);
        Submodel savedSubModel = shellService.save(getDecodedId( aasIdentifier ), toBeSaved);
        return new ResponseEntity<>(submodelMapper.toApiDto(savedSubModel), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> putAssetAdministrationShellDescriptorById( String aasIdentifier, AssetAdministrationShellDescriptor assetAdministrationShellDescriptor ) {
        Shell shell = shellMapper.fromApiDto( assetAdministrationShellDescriptor );
        shellService.update( aasIdentifier, shell.withIdExternal( aasIdentifier ) );
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> putSubmodelDescriptorByIdThroughSuperpath( byte[] aasIdentifier, byte[] submodelIdentifier, SubmodelDescriptor submodelDescriptor ) {
        Submodel submodel = submodelMapper.fromApiDto( submodelDescriptor );
        shellService.update( getDecodedId( aasIdentifier ), getDecodedId( submodelIdentifier ), submodel.withIdExternal( getDecodedId( submodelIdentifier ) ) );
        return new ResponseEntity<>( HttpStatus.NO_CONTENT );
    }

    @Override
    public ResponseEntity<List<String>> getAllAssetAdministrationShellIdsByAssetLink(List<SpecificAssetId> assetIds,
    Integer limit, String cursor) {
        // TODO: Implement cursor based pag.
        if (assetIds == null || assetIds.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        List<String> externalIds = shellService.findExternalShellIdsByIdentifiersByExactMatch(shellMapper.fromApiDto(assetIds));
        return new ResponseEntity<>(externalIds, HttpStatus.OK);
    }

    @Override
        public ResponseEntity<List<SpecificAssetId>> getAllAssetLinksById(byte[] aasIdentifier) {
            Set<ShellIdentifier> identifiers = shellService.findShellIdentifiersByExternalShellId(getDecodedId( aasIdentifier ));
            return new ResponseEntity<>(shellMapper.toApiDto(identifiers), HttpStatus.OK);
        }

        @Override
    public ResponseEntity<List<SpecificAssetId>> postAllAssetLinksById(byte[] aasIdentifier, List<SpecificAssetId> specificAssetId) {
        Set<ShellIdentifier> shellIdentifiers = shellService.save(getDecodedId( aasIdentifier ), shellMapper.fromApiDto(specificAssetId));
        List<SpecificAssetId> list = shellMapper.toApiDto(shellIdentifiers);
        return new ResponseEntity<>(list, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<String>> postQueryAllAssetAdministrationShellIds(ShellLookup shellLookup) {
        List<SpecificAssetId> assetIds = shellLookup.getQuery().getAssetIds();
        List<String> externalIds = shellService.findExternalShellIdsByIdentifiersByAnyMatch(shellMapper.fromApiDto(assetIds));
        return new ResponseEntity<>(externalIds, HttpStatus.OK);
    }
}

