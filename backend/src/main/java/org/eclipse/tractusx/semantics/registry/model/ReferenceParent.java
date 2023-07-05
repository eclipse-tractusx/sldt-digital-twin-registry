package org.eclipse.tractusx.semantics.registry.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

import lombok.Value;

@Value
public class ReferenceParent {

   @Id
   UUID id;

   String type;

   @MappedCollection(idColumn = "fk_reference_parent_id")
   List<ReferenceKey> keys;

}
