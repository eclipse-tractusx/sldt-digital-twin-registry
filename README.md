<!--
    Copyright (c) 2021-2022 T-Systems International GmbH
    Copyright (c) 2021-2022 Contributors to the Eclipse Foundation

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


# Digital Twin Registry
The Digital Twin Registry is a logical and architectural component of Tractus-X.
The source code under this folder contains reference implementations of the SLDT Registry.

## Build Packages
Run `mvn install` to run unit tests, build and install the package.

## Run Package Locally
To check whether the build was successful, you can start the resulting JAR file from the build process by running `java -jar backend/target/digital-twin-registry-backend-{current-version}.jar`.

## Build Docker
Run `docker build -t registry .`

In case you want to publish your image into a remote container registry, apply the tag accordingly and `docker push` the image.

## Install Instructions
For detailed install instructions please refer to our [INSTALL.md](https://github.com/eclipse-tractusx/sldt-digital-twin-registry/blob/main/INSTALL.md)