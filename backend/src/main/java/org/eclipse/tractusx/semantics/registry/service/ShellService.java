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
package org.eclipse.tractusx.semantics.registry.service;

import static org.springframework.data.domain.PageRequest.ofSize;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.RegistryProperties;
import org.eclipse.tractusx.semantics.aas.registry.model.GetAllAssetAdministrationShellIdsByAssetLink200Response;
import org.eclipse.tractusx.semantics.aas.registry.model.PagedResultPagingMetadata;
import org.eclipse.tractusx.semantics.registry.dto.BatchResultDto;
import org.eclipse.tractusx.semantics.registry.dto.ShellCollectionDto;
import org.eclipse.tractusx.semantics.registry.dto.SubmodelCollectionDto;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellMinimal;
import org.eclipse.tractusx.semantics.registry.model.support.DatabaseExceptionTranslation;
import org.eclipse.tractusx.semantics.registry.repository.ShellIdentifierRepository;
import org.eclipse.tractusx.semantics.registry.repository.ShellRepository;
import org.eclipse.tractusx.semantics.registry.repository.SubmodelRepository;
import org.eclipse.tractusx.semantics.registry.utils.ShellCursor;
import org.eclipse.tractusx.semantics.registry.utils.ShellSpecification;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShellService {

    private final ShellRepository shellRepository;
    private final ShellIdentifierRepository shellIdentifierRepository;
    private final SubmodelRepository submodelRepository;
    private final String owningTenantId;

    private final String SORT_FIELD_NAME_SHELL = "createdDate";
   private final String SORT_FIELD_NAME_SUBMODEL = "id";
    private final int MAXIMUM_RECORDS = 1000;

    public ShellService(ShellRepository shellRepository,
                        ShellIdentifierRepository shellIdentifierRepository,
                        SubmodelRepository submodelRepository,
                        RegistryProperties registryProperties) {
        this.shellRepository = shellRepository;
        this.shellIdentifierRepository = shellIdentifierRepository;
        this.submodelRepository = submodelRepository;
        this.owningTenantId = registryProperties.getIdm().getOwningTenantId();
    }

    @Transactional
    public Shell save(Shell shell) {
       if(shellRepository.findByIdExternal(shell.getIdExternal()  ).isPresent()){
          throw new DuplicateKeyException("An AssetAdministrationShell for the given identification does already exists."  );
        }
       return shellRepository.save(shell);
    }

    public void mapShellCollection(Shell shell){
         shell.getIdentifiers().forEach( shellIdentifier -> shellIdentifier.setShellId( shell ) );
         shell.getSubmodels().forEach( submodel -> submodel.setShellId( shell ) );
         shell.getDescriptions().forEach( description -> description.setShellId( shell ) );
         shell.getDisplayNames().forEach( description -> description.setShellId( shell ) );
    }

    @Transactional(readOnly = true)
    public Shell findShellByExternalId(String externalShellId,String externalSubjectId) {
        return shellRepository.findByIdExternal(externalShellId)
                .map(shell -> shell.withIdentifiers(filterSpecificAssetIdsByTenantId(shell.getIdentifiers(), externalSubjectId)))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Shell for identifier %s not found", externalShellId)));
    }

    @Transactional(readOnly = true)
    public ShellCollectionDto findAllShells(Integer pageSize,String cursorVal,String externalSubjectId) {

      pageSize = getPageSize( pageSize );
      ShellCursor cursor = new ShellCursor( pageSize, cursorVal );
      var specification = new ShellSpecification<Shell>( SORT_FIELD_NAME_SHELL, cursor );

      Page<Shell> shellPage = filterSpecificAssetIdsByTenantId( shellRepository.findAll( specification, ofSize( cursor.getRecordSize() ) ),externalSubjectId );
      var shellsPage = shellPage.getContent();

      String nextCursor=null;

      if(shellsPage.size()>0) {
         nextCursor = cursor.getEncodedCursorShell(
               shellsPage.get( shellsPage.size() - 1 ).getCreatedDate(),
               shellPage.hasNext() );
      }

      return ShellCollectionDto.builder()
            .items( shellsPage )
            .cursor( nextCursor )
            .build();
   }

   @Transactional( readOnly = true )
   public SubmodelCollectionDto findAllSubmodel( Integer pageSize, String cursorVal, Shell assetID ) {
      pageSize = getPageSize( pageSize );

      ShellCursor cursor = new ShellCursor( pageSize, cursorVal );
      var specification = new ShellSpecification<Submodel>( SORT_FIELD_NAME_SUBMODEL, cursor );
      Page<Submodel> shellPage = submodelRepository.findAll( Specification.allOf( hasShellFkId( assetID ).and( specification ) ),
            ofSize( cursor.getRecordSize() ) );

      var shellsPage = shellPage.getContent();
      String nextCursor=null;

      if(shellsPage.size()>0) {
         nextCursor = cursor.getEncodedCursorSubmodel(
               shellsPage.get( shellsPage.size() - 1 ).getId(),
               shellPage.hasNext()
         );
      }
      return SubmodelCollectionDto.builder()
            .items( shellsPage )
            .cursor( nextCursor )
            .build();
   }

   private Integer getPageSize( Integer pageSize ) {
      return pageSize == null ? MAXIMUM_RECORDS : pageSize;
   }

   private Specification<Submodel> hasShellFkId( Shell shellId ) {
      return ( root, cq, cb ) -> cb.equal( root.get( "shellId" ), shellId );
   }

    private Page<Shell> filterSpecificAssetIdsByTenantId(Page<Shell> shells,String externalSubjectId){
        return shells.map(shell ->  shell.withIdentifiers(filterSpecificAssetIdsByTenantId(shell.getIdentifiers(), externalSubjectId)));
    }

    private Set<ShellIdentifier> filterSpecificAssetIdsByTenantId(Set<ShellIdentifier> shellIdentifiers, String tenantId) {
        // the owning tenant should always see all identifiers
        if(tenantId.equals(owningTenantId)){
            return shellIdentifiers;
        }
        return shellIdentifiers.stream()
                .filter(shellIdentifier -> shellIdentifier.getExternalSubjectId() == null ||
                        shellIdentifier.getExternalSubjectId().equals(tenantId)).collect(Collectors.toSet());
    }

   @Transactional( readOnly = true )
   public GetAllAssetAdministrationShellIdsByAssetLink200Response findExternalShellIdsByIdentifiersByExactMatch( Set<ShellIdentifier> shellIdentifiers,
         Integer pageSize, String cursor ,String externalSubjectId) {
      List<String> keyValueCombinations = shellIdentifiers.stream().map( shellIdentifier -> shellIdentifier.getKey() + shellIdentifier.getValue() ).toList();

      List<String> queryResult = shellRepository.findExternalShellIdsByIdentifiersByExactMatch( keyValueCombinations,
            keyValueCombinations.size(), externalSubjectId, owningTenantId );
      pageSize = getPageSize( pageSize );

      int startIndex = getCursorDecoded( cursor, queryResult );
      List<String> assetIdList = queryResult.subList( startIndex, queryResult.size() ).stream().limit( pageSize ).collect( Collectors.toList() );

      String nextCursor = getCursorEncoded( queryResult, assetIdList );
      GetAllAssetAdministrationShellIdsByAssetLink200Response response= new GetAllAssetAdministrationShellIdsByAssetLink200Response();
      response.setResult( assetIdList );
      response.setPagingMetadata( new PagedResultPagingMetadata().cursor( nextCursor ) );
      return response;
   }

   private String getCursorEncoded( List<String> queryResult, List<String> assetIdList ) {
      if( queryResult.size()>0) {
         if ( !assetIdList.get( assetIdList.size() - 1 ).equals( queryResult.get( queryResult.size() - 1 ) ) ) {
            String lastEle = assetIdList.get( assetIdList.size() - 1 );
            return Base64.getEncoder().encodeToString( lastEle.getBytes() );
         }
      }
      return null;
   }

   private int getCursorDecoded( String cursor, List<String> queryResult ) {
      if ( cursor != null ) {
         var decodedBytes = Base64.getDecoder().decode( cursor );
         var decodedValue = new String( decodedBytes );
         return queryResult.indexOf( decodedValue ) + 1;
      }
      return 0;
   }

    @Transactional(readOnly = true)
    public List<String> findExternalShellIdsByIdentifiersByAnyMatch(Set<ShellIdentifier> shellIdentifiers,String externalSubjectId) {
        List<String> keyValueCombinations=shellIdentifiers.stream().map( shellIdentifier -> shellIdentifier.getKey()+shellIdentifier.getValue()).toList();

        return shellRepository.findExternalShellIdsByIdentifiersByAnyMatch(keyValueCombinations,externalSubjectId , owningTenantId);
    }

    @Transactional(readOnly = true)
    public List<Shell> findShellsByExternalShellIds(Set<String> externalShellIds,String externalSubjectId) {
        return shellRepository.findShellsByIdExternalIsIn(externalShellIds).stream()
                .map(shell ->  shell.withIdentifiers(filterSpecificAssetIdsByTenantId(shell.getIdentifiers(), externalSubjectId)))
                .collect(Collectors.toList());
    }

   @Transactional
   public void update( Shell shell,String aasIdentifier) {
        deleteShell(  aasIdentifier);
        mapShellCollection( shell );
        try {save( shell );}
        catch ( Exception e ){throw new IllegalArgumentException( e.getMessage() );}
   }
   @Transactional
    public void deleteShell(String externalShellId) {
        ShellMinimal shellFromDb = findShellMinimalByExternalId(externalShellId);
        shellRepository.deleteById(shellFromDb.getId());
    }

    @Transactional(readOnly = true)
    public Set<ShellIdentifier> findShellIdentifiersByExternalShellId(String externalShellId,String externalSubjectId) {
        Shell shellId = findShellByExternalId(externalShellId,externalSubjectId);
        return filterSpecificAssetIdsByTenantId(shellIdentifierRepository.findByShellId(shellId), externalSubjectId);
    }

    @Transactional
    public void deleteAllIdentifiers(String externalShellId) {
        ShellMinimal shellFromDb = findShellMinimalByExternalId(externalShellId);
        shellIdentifierRepository.deleteShellIdentifiersByShellId(shellFromDb.getId(), ShellIdentifier.GLOBAL_ASSET_ID_KEY);
    }

    @Transactional
    public Set<ShellIdentifier> save(String externalShellId, Set<ShellIdentifier> shellIdentifiers,String externalSubjectId) {
        Shell shellFromDb = findShellByExternalId(externalShellId,externalSubjectId);
        shellIdentifierRepository.deleteShellIdentifiersByShellId(shellFromDb.getId(), ShellIdentifier.GLOBAL_ASSET_ID_KEY);

        List<ShellIdentifier> identifiersToUpdate = shellIdentifiers.stream().map(identifier -> identifier.withShellId(shellFromDb))
                .collect(Collectors.toList());
        return ImmutableSet.copyOf(shellIdentifierRepository.saveAll(identifiersToUpdate));
    }

   @Transactional
   public Submodel save( String externalShellId, Submodel submodel, String externalSubjectId ) {
      Shell shellFromDb = findShellByExternalId( externalShellId ,externalSubjectId);
      submodel.setShellId( shellFromDb );
      return saveSubmodel( submodel );
   }

   public Submodel saveSubmodel(Submodel submodel){
      if(submodelRepository.findByShellIdAndIdExternal(submodel.getShellId(),submodel.getIdExternal()).isPresent()){
         throw new DuplicateKeyException("An AssetAdministrationSubmodel for the given identification does already exists."  );
      }
      return submodelRepository.save( submodel );
   }

   @Transactional
   public void update( String externalShellId, Submodel submodel,String externalSubjectId) {
      Shell shellFromDb = findShellByExternalId( externalShellId,externalSubjectId );
      submodel.setShellId( shellFromDb );
      submodel.getDescriptions().forEach( desc-> desc.setSubmodel( submodel ) );
      submodel.getEndpoints().forEach( end -> end.setSubmodel( submodel ) );
      submodelRepository.save(submodel  );
   }

   @Transactional
   public void deleteSubmodel( String externalShellId, String externalSubModelId ,String externalSubjectId) {
      Shell shellFromDb = findShellByExternalId( externalShellId ,externalSubjectId);
      Submodel submodelId = findSubmodelMinimalByExternalId( shellFromDb.getId(), externalSubModelId );
      shellFromDb.getSubmodels().remove( submodelId );
      submodelRepository.deleteById( submodelId.getId() );
   }

    @Transactional(readOnly = true)
    public Submodel findSubmodelByExternalId(String externalShellId, String externalSubModelId,String externalSubjectId) {
        Shell shellIdByExternalId = findShellByExternalId(externalShellId,externalSubjectId);
        return submodelRepository
                .findByShellIdAndIdExternal(shellIdByExternalId, externalSubModelId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Submodel for identifier %s not found.", externalSubModelId)));
    }

   private Submodel findSubmodelMinimalByExternalId( UUID shellId, String externalSubModelId ) {
      Submodel submodel = submodelRepository
            .findMinimalRepresentationByShellIdAndIdExternal( shellId, externalSubModelId )
            .orElseThrow( () -> new EntityNotFoundException( String.format( "Submodel for identifier %s not found.", externalSubModelId ) ) );
      return submodel;
   }

    private ShellMinimal findShellMinimalByExternalId(String externalShellId) {
       return shellRepository.findMinimalRepresentationByIdExternal(externalShellId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Shell for identifier %s not found", externalShellId)));
    }

    /**
     * Saves the provided shells. The transaction is scoped per shell. If saving of one shell fails others may succeed.
     *
     * @param shells the shells to save
     * @return the result of each save operation
     */
    public List<BatchResultDto> saveBatch(List<Shell> shells) {
        return shells.stream().map(shell -> {
            try {
                shellRepository.save(shell);
                return new BatchResultDto("AssetAdministrationShell successfully created.",
                        shell.getIdExternal(), HttpStatus.OK.value());
            } catch (Exception e) {
                if (e.getCause() instanceof DuplicateKeyException) {
                    DuplicateKeyException duplicateKeyException = (DuplicateKeyException) e.getCause();
                    return new BatchResultDto(DatabaseExceptionTranslation.translate(duplicateKeyException),
                            shell.getIdExternal(),
                            HttpStatus.BAD_REQUEST.value());
                }
                return new BatchResultDto(String.format("Failed to create AssetAdministrationShell %s",
                        e.getMessage()), shell.getIdExternal(), HttpStatus.BAD_REQUEST.value());
            }
        }).collect(Collectors.toList());
    }

}
