<!--
    Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
    Copyright (c) 2024 Contributors to the Eclipse Foundation
    See the NOTICE file(s) distributed with this work for additional 
    information regarding copyright ownership.
    
    This program and the accompanying materials are made available under the
    terms of the Apache License, Version 2.0 which is available at
    https://www.apache.org/licenses/LICENSE-2.0.
     
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations
    under the License.
    
    SPDX-License-Identifier: Apache-2.0
-->


# DTR-EDC Access control extension
The goal of this edc extension is to enforce authorized access to submodel implementations according to AAS and Catena-X concluded contracts.

## Configuration Settings

The following table outlines the configuration settings for the `DTR Data Plane Access Control Config` library.

| Configuration Key                                   | Description                                                                                               |
|-----------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `edc.granular.access.verification.edc.data.plane.baseUrl` | Contains the base URL of the EDC data plane endpoint where data plane requests are sent by end users.    |
| `edc.granular.access.verification.dtr.names`             | Comma-separated list of DTR configuration names used as keys for DTR clients.                            |
| `edc.granular.access.verification.dtr.config.`            | Prefix for individual DTR configurations.                                                                 |
| `dtr.decision.cache.duration.minutes`                      | Configuration for the DTR decision cache duration in minutes. The cache is turned off if set to 0.       |
| `aspect.model.url.pattern`                                  | Configuration property suffix for the pattern to recognize aspect model requests needing DTR control.    |
| `dtr.access.verification.endpoint.url`                      | Configuration property suffix for the URL where DTR can be reached.                                      |
| `oauth2.token.endpoint.url`                                 | Configuration property suffix for the URL where OAUTH2 tokens can be obtained for DTR requests.          |
| `oauth2.token.scope`                                        | Configuration property suffix for the scope needed for OAUTH2 token requests to access DTR.              |
| `oauth2.token.clientId`                                     | Configuration property suffix for the client ID used for OAUTH2 token requests to access DTR.            |
| `oauth2.token.clientSecret.path`                            | Configuration property suffix for the path to find the client secret in vault for OAUTH2 token requests. |

## Compatibility

| dtr-edc Extension library version | Digital Twin Registry image version | EDC version |
|-------------------------------|-------------------------------------|-------------|
| `0.1.0`                       | `> 0.4.2`                            | `0.7.X`     |

## Using the DTR-EDC Access Control Extension

To use the EDC extension, you need to build your own EDC Dataplane image. The EDC team provides a GitHub repository template for this purpose. You must fork or copy this repository. More details can be found at [this link](https://github.com/eclipse-tractusx/tractusx-edc-template).

After forking the repository, navigate to the `runtimes/dataplane` folder and open the `build.gradle.kts` file. Here, you can integrate the DTR-EDC Access Control extension and build your own custom dataplane image.

1. Add the following dependency to the dependencies block:

```
implementation ("org.eclipse.tractusx.digital_twin_registry:dtr-edc-access-control-extension:Version")
```

An example looks like:
```
dependencies {
    implementation ("org.eclipse.tractusx.digital_twin_registry:dtr-edc-access-control-extension:0.1.0-RC3")
    runtimeOnly(libs.tx.dataplane) {
        // add module exclusions here as you need them, for example, to exclude the S3 Dataplane features
        // exclude(group = "org.eclipse.edc", module="data-plane-aws-s3")
    }
}
```

2. After adding the dependency, build the Docker image. Execute the following command at the root level:
```
./gradlew dockerize
```

3. To configure the EDC Dataplane, you can provide the parameters mentioned above as environment variables.
   An example in the`values.yaml` file of the tractusx-connector might look like this:
```
...
tractusx-connector:
...
    dataplane:
    ...
      env:
        EDC_GRANULAR_ACCESS_VERIFICATION_ERROR_ENDPOINT_PORT: 9054
        EDC_GRANULAR_ACCESS_VERIFICATION_EDC_DATA_PLANE_BASEURL: http://local-edc-data-plane:9051/public/v2/
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_NAMES: default
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_CONFIG_DEFAULT_DTR_DECISION_CACHE_DURATION_MINUTES: 1
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_CONFIG_DEFAULT_DTR_ACCESS_VERIFICATION_ENDPOINT_URL: http://baseurl-dtr/v2/api/v3/submodel-descriptor/authorized
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_CONFIG_DEFAULT_ASPECT_MODEL_URL_PATTERN: http:\/\/baseurl-submodelserver\/pcf\/.*
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_CONFIG_DEFAULT_OAUTH2_TOKEN_ENDPOINT_URL: http://baseurl-keycloak/iam/access-management/v1/tenants/00000000-0000-0000-0000-000000000000/openid-connect/token
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_CONFIG_DEFAULT_OAUTH2_TOKEN_SCOPE: aud:local-edc-dtr
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_CONFIG_DEFAULT_OAUTH2_TOKEN_CLIENTID: dtr_client
        EDC_GRANULAR_ACCESS_VERIFICATION_DTR_CONFIG_DEFAULT_OAUTH2_TOKEN_CLIENTSECRET_PATH: dtrsecret
    ...
```

4. This custom dataplane docker image can now be deployed.