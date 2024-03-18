###############################################################################
# Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
# Copyright (c) 2021 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

import json
import uuid
import urllib.parse
import base64


from locust import HttpUser, task, constant_throughput
from locust.exception import RescheduleTask

class AasRegistryTask(HttpUser):
    
    # If 100 Users are configured in the load test
    # constant_throughput ensures that: 100 Users * 0.1 = 10 request/s
    wait_time = constant_throughput(0.1)

    @task
    def createAndQueryAasDescriptor(self):
        shell = generate_shell()
        headers = { 'Content-Type' : 'application/json', 'Edc-Bpn' : 'BPN_COMPANY_1'}
        with self.client.post("/api/v3/shell-descriptors", data=json.dumps(shell), headers= headers, catch_response=True) as response:
            if response.status_code != 201:
                response.failure(f"Expected 201 but status code was {response.status_code}")
                raise RescheduleTask()

        shell_id = shell['id']
        shell_id_encoded = str(base64.urlsafe_b64encode(shell_id.encode("utf-8")), "utf-8")

        with self.client.get(f"/api/v3/shell-descriptors/{shell_id_encoded}", name = "/api/v3/shell-descriptors/{id}", headers= headers, catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"Expected 200 but status code was {response.status_code}")
                raise RescheduleTask()

        specificAssetIds = shell['specificAssetIds']
        decodedAssetIds = urllib.parse.quote_plus(json.dumps(specificAssetIds))
        with self.client.get(f"/api/v3/lookup/shells?assetIds={decodedAssetIds}", name = "/api/v3/lookup/shells?assetIds={assetIds}", headers= headers, catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"Expected 200 but status code was {response.status_code}")
                raise RescheduleTask()

def generate_shell():
    aasId = uuid.uuid4()
    globalAssetId = uuid.uuid4()
    specificAssetId1 = uuid.uuid4()
    specificAssetId2 = uuid.uuid4()
    specificAssetId3 = uuid.uuid4()
    specificAssetId4 = uuid.uuid4()
    submodelId1 = uuid.uuid4()
    submodelId2 = uuid.uuid4()
    return {
             "idShort": "idShortExample",
             "id": str(aasId),
             "description": [
               {
                 "language": "de",
                 "text": "example text"
               }
             ],
             "displayName": [
               {
                 "language": "de",
                 "text": "this is an example description1"
               }
             ],
             "specificAssetIds":[
                 {
                    "name":"assetLifecyclePhase",
                    "value":str(specificAssetId1),
                    "externalSubjectId":{
                       "type":"ExternalReference",
                       "keys":[
                          {
                             "type":"Submodel",
                             "value":"PUBLIC_READABLE"
                          }
                       ]
                    }
                 },
                 {
                    "name":"CustomerPartId",
                    "value":str(specificAssetId3),
                    "externalSubjectId":{
                       "type":"ExternalReference",
                       "keys":[
                          {
                             "type":"Submodel",
                             "value":"BPN_COMPANY_1"
                          }
                       ]
                    }
                 },
                 {
                    "name":"Serialnr",
                    "value":str(specificAssetId4),
                    "externalSubjectId":{
                       "type":"ExternalReference",
                       "keys":[
                          {
                             "type":"Submodel",
                             "value":"BPN_COMPANY_1"
                          },
                          {
                             "type":"Submodel",
                             "value":"BPN_COMPANY_2"
                          },
                          {
                             "type":"Submodel",
                             "value":"BPN_COMPANY_3"
                          }
                       ]
                    }
                 }
              ],
             "submodelDescriptors": [
               {
                 "id": str(submodelId1),
                 "endpoints": [
                   {
                     "interface": "interfaceNameExample",
                     "protocolInformation": {
                       "href": "endpointAddressExample",
                       "endpointProtocol": "endpointProtocolExample",
                       "endpointProtocolVersion": [
                         "e"
                       ],
                       "subprotocol": "5hg",
                       "subprotocolBody":"",
                       "subprotocolBodyEncoding": "subprotocolBodyExample",
                       "securityAttributes": [
                         {
                           "type": "NONE",
                           "key": "sec",
                           "value": "1"
                         }
                       ]
                     }
                   }
                 ],
                 "idShort": "idShortExample",
                 "semanticId": {
                   "type": "ExternalReference",
                   "keys": [
                     {
                       "type": "Submodel",
                       "value": "urn:bamm:io.catenax.serial_part_typization:1.1.0#SerialPartTypization"
                     }
                   ]
                 },
                 "description": [
                   {
                     "language": "de",
                     "text": "hello text"
                   },
                   {
                     "language": "en",
                     "text": "hello s"
                   }
                 ]
               },
               {
                 "id": str(submodelId2),
                 "endpoints": [
                   {
                     "interface": "interfaceNameExample",
                     "protocolInformation": {
                       "href": "endpointAddressExample",
                       "endpointProtocol": "endpointProtocolExample",
                       "endpointProtocolVersion": [
                         "e"
                       ],
                       "subprotocol": "5hg",
                       "subprotocolBody":"",
                       "subprotocolBodyEncoding": "subprotocolBodyExample",
                       "securityAttributes": [
                         {
                           "type": "NONE",
                           "key": "sec",
                           "value": "1"
                         }
                       ]
                     }
                   }
                 ],
                 "idShort": "idShortExample",
                 "semanticId": {
                   "type": "ExternalReference",
                   "keys": [
                     {
                       "type": "Submodel",
                       "value": "urn:bamm:io.catenax.serial_part_typization:1.1.0#SerialPartTypization"
                     }
                   ]
                 },
                 "description": [
                   {
                     "language": "de",
                     "text": "hello text"
                   },
                   {
                     "language": "en",
                     "text": "hello s"
                   }
                 ]
               }
             ]
           }