package org.eclipse.tractusx.semantics.registry.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import lombok.Value;

@Value

public class ShellDisplayName {
   @Id
   UUID id;
   String language;
   String text;
}
