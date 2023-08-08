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
For detailed install instructions please refer to our [INSTALL.md](./INSTALL.md)

## Notice for Docker image

DockerHub:  [Docker.io:tractusx-digital-twin-registry](https://hub.docker.com/r/tractusx/sldt-digital-twin-registry) <br/>
The Docker images from version 0.3.3-M1 until the latest can be found on DockerHub, older versions have been published on GitHub Container Registry (GHCR): [catena-ng:digital-twin-registry](https://github.com/catenax-ng/product-semantics/pkgs/container/sldt-digital-twin-registry)

The Helm chart can be found at the GitHub repository at: [Releases](https://github.com/eclipse-tractusx/sldt-digital-twin-registry/releases) <br/>
This application provides container images for demonstration purposes.
Eclipse Tractus-X product(s) installed within the image:

- GitHub: https://github.com/eclipse-tractusx/sldt-digital-twin-registry
- Project home: https://projects.eclipse.org/projects/automotive.tractusx
- Dockerfile: https://github.com/eclipse-tractusx/sldt-digital-twin-registry/blob/main/backend/Dockerfile
- Project license: [Apache License, Version 2.0](https://github.com/eclipse-tractusx/sldt-digital-twin-registry/blob/main/LICENSE)

**Used base image**
- [eclipse-temurin:17-jre-alpine](https://github.com/adoptium/containers)
- Official Eclipse Temurin DockerHub page: https://hub.docker.com/_/eclipse-temurin  
- Eclipse Temurin Project: https://projects.eclipse.org/projects/adoptium.temurin  
- Additional information about the Eclipse Temurin images: https://github.com/docker-library/repo-info/tree/master/repos/eclipse-temurin

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.