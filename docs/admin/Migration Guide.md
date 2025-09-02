## [0.9.0](https://github.com/eclipse-tractusx/sldt-digital-twin-registry/releases/tag/digital-twin-registry-0.9.0)
- AAS 3.1 release - 
    - The GetAllAssetAdministrationShellIdsByAssetLink (GET ‘/lookup/shells’ ) service has been deprecated in the AAS 3.1 release. Please note that there are no breaking changes for consumers, and the deprecated service remains available for use.
    - The POST /lookup/shellsByAssetLink endpoint has been available since March 2024. Only the operationId updated from SearchAllShellsByAssetLink to SearchAllAssetAdministrationShellIdsByAssetLink as part of the AAS 3.1 release.
    - There have been updates to field sizes and regular expression patterns for idShort, among other changes. A detailed summary of the AAS 3.1 release changes is documented in tabular format [here](https://github.com/eclipse-tractusx/sldt-digital-twin-registry/issues/524)
    - Provider need to follow the idShort regex pattern while creating Asset Administration Shells (AAS) and Submodels. The old idShort remains valid for backward compatibility.
    - Liquibase scripts have been updated with the respective field size changes in the AAS 3.1 release.