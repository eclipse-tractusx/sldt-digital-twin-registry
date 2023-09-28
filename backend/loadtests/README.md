<!--
    Copyright (c) 2021-2022 Robert Bosch Manufacturing Solutions GmbH
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

# Introduction

This folder contains the load tests for the AAS Registry.
The tool used for the load testing is `https://locust.io/`.

# Load test

The current implemented load test does the following:

   - Creates a shell
   - Retrieves the created shell by id
   - Lookups the shell by specific asset ids

To reduce the complexity, authentication is disabled.

# Executing the test

The `docker-compose.yml` all relevant services to execute the load test.

   - AAS Registry (latest INT version)
   - PostgreSQL Database as persistence for the AAS Registry
   - Locust Master for the Webui
   - Locust Worker for the load test execution

# Run the load test

   1. Execute `docker-compose up -d`
   2. Open the Locust WebUI `http://localhost:8090`
   3. In the opened form enter the following:
         - Number of users = 100 (=> 10 req/s)
         - Spawn rate      = 5
         - Host            = http://host.docker.internal:4243
   4. Press Start. Locust will now execute the load test as long as you wish.
   5. You can stop the test at anytime through the UI and grab the statistics.

# Local development

The steps for local development of the load tests are:

   1. Ensure python3 is installed
   2. Run `pip3 install -r requirements.txt`
   3. Modify the script
   4. Run `locust -f ./locust/locustfile.py --headless --users 1 --spawn-rate 1 -H http://host.docker.internal:4243`
