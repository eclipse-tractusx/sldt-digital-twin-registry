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


