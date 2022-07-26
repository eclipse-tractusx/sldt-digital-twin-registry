# Version: v0.0.1-M1
The initial implementation of the AssetAdministrationShell Registry API. 
    - Support for all API endpoints from the official AssetAdministrationShell Registry API specification


# Version: v0.0.1-M1-central (unofficial release)
Implements tenant based access control.
The application takes the tenantId from the JWT token and assigns to the AssetAdministrationShellDescriptor.

Authorization rules:
    - Only the owning tenant of a ShellDescriptor can update / delete it and any of its subresource
    - Read access is allowed across all tenants (tenant a can read the resources of tenant b)