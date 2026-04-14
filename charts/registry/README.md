# digital-twin-registry

![Version: 0.11.0](https://img.shields.io/badge/Version-0.11.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 0.11.0](https://img.shields.io/badge/AppVersion-0.11.0-informational?style=flat-square)

Tractus-X Digital Twin Registry Helm Chart

**Homepage:** <https://eclipse-tractusx.github.io/>

## Source Code

* <https://github.com/eclipse-tractusx/sldt-digital-twin-registry>

## Requirements

| Repository | Name | Version |
|------------|------|---------|
| oci://registry-1.docker.io/cloudpirates | postgres (alias: postgresql) | 0.11.0 |
| oci://registry-1.docker.io/cloudpirates | keycloak | 0.13.6 |

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- PV provisioner support in the underlying infrastructure

## Install

To install the chart with the release name `registry`:

```shell
$ helm repo add tractusx-dev https://eclipse-tractusx.github.io/charts/dev
$ helm install registry tractusx-dev/registry
```
To install the helm chart into your cluster with your values:

```shell
$ helm install -f your-values.yaml registry tractusx-dev/registry
```

To use the helm chart as a dependency:

```yaml
dependencies:
  - name: registry
    repository: https://eclipse-tractusx.github.io/charts/dev
    version: YOUR_VERSION
```

To install the local version in the namespace _semantics_:

```shell

helm dependency update .

helm install registry -n semantics . --create-namespace
```

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| enableKeycloak | bool | `false` |  |
| enablePostgres | bool | `true` |  |
| fullnameOverride | string | `nil` |  |
| keycloak.image.tag | string | `"26.0.7"` | Official Keycloak image tag (CloudPirates) |
| keycloak.keycloak.adminUser | string | `"admin"` | Keycloak admin username |
| keycloak.keycloak.adminPassword | string | `"admin"` | Keycloak admin password |
| keycloak.keycloak.proxyHeaders | string | `"xforwarded"` | Proxy header mode (was `proxy: edge` in Bitnami) |
| keycloak.keycloak.production | bool | `false` | Production mode |
| keycloak.keycloak.httpRelativePath | string | `"/auth"` | Context path (no trailing slash!) |
| keycloak.database.type | string | `"postgres"` | Database type |
| keycloak.database.host | string | `"registry-postgresql"` | Database host (same PostgreSQL instance as DTR) |
| keycloak.database.port | string | `"5432"` | Database port |
| keycloak.database.name | string | `"default-database"` | Database name |
| keycloak.database.existingSecret | string | `"secret-dtr-postgres-init"` | Secret with `db-username` and `db-password` keys |
| keycloak.postgres.enabled | bool | `false` | Disable internal postgres subchart |
| keycloak.realm.import | bool | `true` | Enable realm import (appends `--import-realm` to start command) |
| keycloak.service.type | string | `"ClusterIP"` | Service type |
| keycloak.fullnameOverride | string | `"registry-keycloak"` | Override for stable service names |
| keycloak.extraVolumeMounts[0].mountPath | string | `"/opt/keycloak/data/import/default-realm-import.json"` | Realm import file mount path |
| keycloak.extraVolumeMounts[0].name | string | `"registry-keycloak-configmap"` | Volume name |
| keycloak.extraVolumeMounts[0].subPath | string | `"default-realm-import.json"` | File name within ConfigMap |
| keycloak.extraVolumes[0].configMap.name | string | `"registry-keycloak-configmap"` | ConfigMap name for realm import |
| keycloak.extraVolumes[0].name | string | `"registry-keycloak-configmap"` | Volume name |
| nameOverride | string | `nil` |  |
| postgresql.auth.database | string | `"default-database"` |  |
| postgresql.auth.existingSecret | string | `"secret-dtr-postgres-init"` | Secret contains passwords for username postgres. |
| postgresql.auth.password | string | `nil` |  |
| postgresql.auth.username | string | `"default-user"` |  |
| postgresql.primary.persistence.enabled | bool | `true` |  |
| postgresql.primary.persistence.size | string | `"50Gi"` |  |
| postgresql.service.ports.postgresql | int | `5432` |  |
| registry.authentication | bool | `true` |  |
| registry.containerPort | int | `4243` |  |
| registry.dataSource.driverClassName | string | `"org.postgresql.Driver"` |  |
| registry.dataSource.password | string | `nil` |  |
| registry.dataSource.sqlInitPlatform | string | `"pg"` |  |
| registry.dataSource.url | string | `"jdbc:postgresql://database:5432"` |  |
| registry.dataSource.user | string | `"default-user"` |  |
| registry.externalSubjectIdWildcardAllowedTypes | string | `"manufacturerPartId,digitalTwinType"` |  |
| registry.externalSubjectIdWildcardPrefix | string | `"PUBLIC_READABLE"` |  |
| registry.granularAccessControlFetchSize | string | `"500"` |  |
| registry.host | string | `"minikube"` |  |
| registry.identityProvider | string | `"keycloak"` |  |
| registry.idpClientId | string | `"default-client"` |  |
| registry.idpInternalClientId | string | `"default-client"` |  |
| registry.idpIssuerUri | string | `""` |  |
| registry.image.registry | string | `"docker.io"` |  |
| registry.image.repository | string | `"tractusx/sldt-digital-twin-registry"` |  |
| registry.image.version | string | `""` |  |
| registry.imagePullPolicy | string | `"IfNotPresent"` |  |
| registry.ingress.annotations | object | `{}` |  |
| registry.ingress.className | string | `"nginx"` |  |
| registry.ingress.enabled | bool | `true` |  |
| registry.ingress.rules | list | `[]` |  |
| registry.ingress.tls.enabled | bool | `true` |  |
| registry.ingress.tls.secretName | string | `"registry-certificate-secret"` |  |
| registry.ingress.urlPrefix | string | `"/semantics/registry"` |  |
| registry.livenessProbe.failureThreshold | int | `3` |  |
| registry.livenessProbe.initialDelaySeconds | int | `100` |  |
| registry.livenessProbe.periodSeconds | int | `3` |  |
| registry.podSecurityContext.runAsUser | int | `100` |  |
| registry.readinessProbe.failureThreshold | int | `3` |  |
| registry.readinessProbe.initialDelaySeconds | int | `100` |  |
| registry.readinessProbe.periodSeconds | int | `3` |  |
| registry.replicaCount | int | `1` |  |
| registry.resources.limits.cpu | string | `"750m"` |  |
| registry.resources.limits.memory | string | `"1024Mi"` |  |
| registry.resources.requests.cpu | string | `"250m"` |  |
| registry.resources.requests.memory | string | `"1024Mi"` |  |
| registry.securityContext.allowPrivilegeEscalation | bool | `false` |  |
| registry.securityContext.readOnlyRootFilesystem | bool | `true` |  |
| registry.securityContext.runAsUser | int | `100` |  |
| registry.service.port | int | `8080` |  |
| registry.service.type | string | `"ClusterIP"` |  |
| registry.tenantId | string | `"default-tenant"` |  |
| registry.useGranularAccessControl | string | `"false"` |  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.11.3](https://github.com/norwoodj/helm-docs/releases/v1.11.3)

## NOTICE

This work is licensed under the [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0).

- SPDX-License-Identifier: Apache-2.0
- SPDX-FileCopyrightText: 2021 Contributors to the Eclipse Foundation
- Source URL: https://github.com/eclipse-tractusx/sldt-digital-twin-registry
