package org.eclipse.tractusx.semantics.registry.model;

public enum ShellKind {
   INSTANCE("Instance"),

   NOTAPPLICABLE("NotApplicable"),

   TYPE("Type");

   private String kind;

   ShellKind(String kind) {
      this.kind = kind;
   }
}
