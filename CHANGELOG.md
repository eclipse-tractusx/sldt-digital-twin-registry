# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.3.18
### Added
- Update PostgreSQL version to 15.4.

## fixed
- Fixed CVE-2023-36478 | CWE-190 and CVE-2023-40167| CWE-130 Third-Party Components vulnerability.

## 0.3.17
### Added

## fixed
- Performance improvements in the look-up API - GET /lookup/shells.
- Implemented the regex patterns for various fields. 
- IdShort is unique on shell level. Idshort can be exists only one time in the database (tenant).IdShort of submodelDescriptor is unique inside a shell. Uniqueness on shellId and idShort and Idshort of submodelDescriptors are unique.

## 0.3.16-M1
### Added

## fixed
- change comment style in .tractusx to yaml style (#).


## 0.3.15-M1
### Added

## fixed
- Add deprecated api `/query` for release 3.2. The Api `query` is not a part of AAS 3.0. But for release 3.2, this api will be added and marked as deprecated. This api will be removed in future releases.

## 0.3.14-M1
### Added

## fixed
- Bugfix: GlobalAssetId was not shown in the shell response. This bug is fixed. GlobalAssetId is shown, if the consumer has full access to the shell.

## 0.3.13-M1
### Added
- In this version the models have been adjusted to new version AAS 3.0.
- Implement access control to Digital Twins Based on the BPN (Business Partner Number)/ TenantId

## fixed
- Refactored existing models to align with AAS 3.0. Removed /query from lookup api because it is not a part of AAS 3.0 version.

## 0.3.12-M1
### Added

## fixed
- Fix CVE-2023-34035 (update springboot to version 3.1.2)
- Fix CVE-2023-2976 (update google guava to version 32.1.1-jre)

## 0.3.11-M1
### Added

## fixed
- In this version, a bug is fixed that occurred while fetching all the submodels for a Shell.

## 0.3.10-M1
### Added
- In this new version, Base64 decoding is provided for the provided encoded parameters. All the provided path parameters has to be Base64 URL encoded.

## fixed


## 0.3.9-M1
### Added
- In this new version, Cursor pagination is provided for search instead of classical offset pagination in previous version.

## fixed
- In this version, Spring-jdbc is converted to spring-jpa.

## 0.3.8-M1
### Added
- Update functionality to get bpn from HEADER (Edc-Bpn) instead of token and use it to search for specificAssetIds.

## fixed
- In this version, fix is given for adjusting SemanticID in submodel response. This is only temporary workaround.

## 0.3.7-M1
### Added

## fixed
- In this version, fix is given for adjusting openapiyml for length of subprotocolbody field to 2000 from 128 char.

## 0.3.6-M1
### Added
- This version includes changes for updating the subprotocolbody length from 50 to 2000 char.
## fixed

## 0.3.5-M1
This version includes changes for updating the application to AAS version 3.0.
The old AAS version is no longer supported. Only the API version 3.0 is supported.
### Added
- Update openapi yml file to the newest version AAS 3.0 version
- Remove old AAS version.
## fixed

## 0.3.4-M1
This version includes changes for the decentralized digital twin registry.
### Added
- Move Veracode to eclipse-tractusX
## fixed

## 0.3.3-M1
This version includes changes for the decentralized digital twin registry.
### Added

## fixed
- Update INSTALL.md instruction to adapt /etc/hosts

## 0.3.2-M1
This version includes changes for the decentralized digital twin registry.
### Added

## fixed
- fix CVE-2023-20862
- fix CVE-2023-20873

## 0.3.1-M1
This version includes changes for the decentralized digital twin registry.
### Added
- Create INSTALL.md
- Rename documentation.md to README.md
- Add .tractusx file
- CODE_OF_CONDUCT.md

## fixed

## 0.2.0-M13-multi-tenancy
### Added
- Provide functionality to return only specificAssetIds for consumer (not owner of twins) which matched the externalSubjectIds

## fixed

## 0.2.0-M12-multi-tenancy
### Added
- Create INSTALL.md
- Rename documentation.md to README.md
- Add .tractusx file
- CODE_OF_CONDUCT.md

## fixed

## 0.2.0-M11-multi-tenancy
### Added

## fixed
- Update dependencies.md

## 0.2.0-M10-multi-tenancy
### Added

## fixed
- Fix cve-2023-20863 (update spring-core to 6.0.8)

## 0.2.0-M9-multi-tenancy
### Added

## fixed
- Fix cve-2023-20863 (update spring-expression to 6.0.8)

## 0.2.0-M8-multi-tenancy
### Added

## fixed
- Fix cve-2022-45688 (update json to 20230227)

## 0.2.0-M7-multi-tenancy
### Added
- Update application to springboot 3.0.5

## fixed
- Fix vulnerability spring-expression

## 0.2.0-M6-multi-tenancy
### Added
- Update application to springboot 3.0.1

## 0.2.0-M5-multi-tenancy
### Added
- Move backend/deployment to separate charts folder
- Add new workflow for Helm chart releases
- Include action to parse dependency licenses
- Making image version configurable and introduce appVersion as default

### fixed
- Fixing CVEs (2022-41946 and CVE-2022-41854)

## 0.2.0-M4-multi-tenancy
### Fixed
- Increase jackson-databind version to 2.14.0
- Fixing cve-2022-31692
- Adjusting openapi yaml to fix validation error

## 0.2.0-M3-multi-tenancy
### fixed
- Update commons-text (apache-org.apache.commons) to 1.10.0

## 0.2.0-M2-multi-tenancy
### fixed
- Clean-up POM
- Increase Spring Boot version
- Update Docker base images
- Adjust snakeyaml version to 1.31
- Remove h2 database from packaged JAR

## 0.2.0-M1-multi-tenancy
### Added
- Replace variable tenantId by the tenantId from the database
### fixed
- Fixed broken tests

## 0.1.0-M1-multi-tenancy
### Added
- The registry prevents access to specificAssetIds by evaluating the BPN of a user
- The registry enforces that only users with the same BPN can update or delete twin entries once created (multi-tenancy)
- Helm Charts available via helm Repository at Eclipse Foundation
- Swagger UI now integrated with Portal Authentication

### fixed

### Changed
- Update of Spring Boot version to 2.7.3

## 0.0.1-M1
### Added
- The digital twin registry allows data consumers to find data endpoints and connect to them
- The digital twin registry allows data providers the creation, update, deletion of digital twin entries
- Twins contain IDs, local identifiers and a number of submodel entries
- Submodels contain a link to the data endpoint and reference a semantic model description of how the data is structured
- The registry prevents unauthenticated access by checking whether an access token is provided by a CX user
- The registry enforces that only users with the correct role can read/create/update/delete twin entries