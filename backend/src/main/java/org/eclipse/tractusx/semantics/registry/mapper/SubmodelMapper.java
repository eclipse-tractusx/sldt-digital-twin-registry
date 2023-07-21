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
package org.eclipse.tractusx.semantics.registry.mapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.tractusx.semantics.aas.registry.model.*;
import org.eclipse.tractusx.semantics.registry.dto.SubmodelCollectionDto;
import org.eclipse.tractusx.semantics.registry.model.*;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubmodelMapper {
    @Mappings({
            @Mapping(target="idExternal", source="id"),
            @Mapping(target = "descriptions", source = "description"),
            @Mapping(target="semanticId", source = "semanticId"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "displayNames", source = "displayName"),
            @Mapping(target = "submodelExtensions", source = "extensions"),
            @Mapping(target = "submodelSupplemSemanticIds", source = "supplementalSemanticId")
    })
    Submodel fromApiDto(SubmodelDescriptor apiDto);

   SubmodelDescription mapShellDescription (LangStringTextType description);

   @Mappings({
         @Mapping(target="submodSemanticId", source = "semanticId"),
         @Mapping(target="submodSupplementalIds", source = "supplementalSemanticIds"),
         @Mapping(target="name", source = "name"),
         @Mapping(target="valueType", source = "valueType"),
         @Mapping(target="value", source = "value"),
         @Mapping(target="refersTo", source = "refersTo")
   })
   SubmodelExtension mapSubmodelExtension (Extension submodelExtensions);

   @Mappings({
            @Mapping(target="interfaceName", source = "interface"),
            @Mapping(target="endpointAddress", source = "protocolInformation.href"),
            @Mapping(target="endpointProtocol", source = "protocolInformation.endpointProtocol"),
            @Mapping(target="subProtocol", source = "protocolInformation.subprotocol"),
            @Mapping(target="subProtocolBody", source = "protocolInformation.subprotocolBody"),
            @Mapping(target="subProtocolBodyEncoding", source = "protocolInformation.subprotocolBodyEncoding"),
            @Mapping(target = "endpointProtocolVersion", source = "protocolInformation.endpointProtocolVersion" , qualifiedByName = "endpointProtocolVersionMapping"),
            @Mapping( target = "submodelSecurityAttribute", source = "protocolInformation.securityAttributes")
    })
    SubmodelEndpoint fromApiDto(Endpoint apiDto);

    @Named("endpointProtocolVersionMapping")
    default String endpointProtocolVersion(List<String> endpointProtocolVersions) {
       return Optional.ofNullable(endpointProtocolVersions).map(endpointPVs -> String.join(",", endpointPVs)).orElse(null);
    }

    @Named("protocolVersionDescriptor")
    default List<String>  protocolVersionDescriptor(String version){
        List<String> versions= Stream.of(version.split(","))
              .map(String::trim)
                .collect( Collectors.toList());
        return versions;
    }

   @Mappings({
         @Mapping(source = "items", target = "result"),
         @Mapping(source = "cursor", target = "pagingMetadata.cursor"),
   })
   GetSubmodelDescriptorsResult toApiDto( SubmodelCollectionDto shell);

    @Mappings({
            @Mapping(source = "endpointProtocolVersion", target = "protocolInformation.endpointProtocolVersion" , qualifiedByName = "protocolVersionDescriptor"),
          @Mapping( source = "submodelSecurityAttribute", target = "protocolInformation.securityAttributes")
    })
    @InheritInverseConfiguration
    Endpoint toApiDto(SubmodelEndpoint apiDto);

    @InheritInverseConfiguration
    SubmodelDescriptor toApiDto(Submodel shell);

   LangStringTextType mapSubModelDescription (SubmodelDescription description);

   @Mappings({
         @Mapping(source = "submodelExtensions", target = "extensions"),
   })
   @InheritInverseConfiguration
   List<SubmodelDescriptor> toApiDto( Set<Submodel> submodels );

   @Mappings({
         @Mapping(source="submodSemanticId", target = "semanticId"),
         @Mapping(source="submodSupplementalIds", target = "supplementalSemanticIds"),
         @Mapping(source="name", target = "name"),
         @Mapping(source="valueType", target = "valueType"),
         @Mapping(source="value", target = "value"),
         @Mapping(source="refersTo", target = "refersTo")
   })
   Extension mapExtension (SubmodelExtension submodelExtension);
}
