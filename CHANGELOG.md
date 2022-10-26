# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.1.0-M1-multitenancy
### Added
- The registry prevents access to specificAssetIds by evaluating the BPN of a user
- The registry enforces that only users with the same BPN can update or delete twin entries once created (multi-tenancy)
- Helm Charts available via helm Repository at Eclipse Foundation
- Swagger UI now integrated with Portal Authentication

 ### fixed
- Update jackson-databind to version 2.13.3 (A1SLDT-952)
- Update commons-text (apache-org.apache.commons) to 1.10.0

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