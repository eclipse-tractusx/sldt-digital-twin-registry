package org.eclipse.tractusx.semantics.registry.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import lombok.Value;
import lombok.With;

@Value
@With
public class SecurityAttribute {

   @Id
   UUID id;

   private SecurityType type;

   private String key;

   private String value;
}
