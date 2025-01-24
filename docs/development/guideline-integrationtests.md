# Guideline to use/add usecase for integration tests
This documentation outlines the structure and conventions used in the integration tests for the application. 

## Test location
All integration testcases are localized under the following directory:
``
src/test/resources/integrationtests
``

## Test structure
Each usecase for integration testing has its own directory within the integrationtests folder. 
Each directory is named according to the specific usecase tests and contains subdirectories that represent individual actions within the use case. 
The general structure is as follows:
```
integrationtests
    aas-registry-usecases/
        │
        └───usecase1/
        │   │
        │   └───1_action/
        │   │   │   request.json
        │   │   │   expected-response.json
        │   │   
        │   └───2_action/
        │       │   request.json
        │       │   expected-response.json
        │
        └───usecase2/
        │
        (similar structure as usecase1)
    (similar structure as aas-registry-usecases)/
```

## Description of Directory and Files
### Usecase Directory 
Each use case has a dedicated directory (usecase1, etc.) . This directory may contain multiple action directories.

### Action Directory
Inside each use case directory, there are subdirectories for each action. 
Each action directory corresponds to a specific step or operation within the use case.
Number the actions so that they are performed sequentially.

### Files
#### request.json
This file contains the JSON-formatted request payload that is sent to the system being tested. It should accurately represent the input the system will receive during the test.
The json has the following parameters:

| Field    | Description                           |
|----------|---------------------------------------|
| `url`    | URL of the API                        |
| `tenant` | EDC-BPN which will perform the request|
| `method` | Method type (POST, DELETE, GET, UPDATE) |
| `body`   | request body                          |

Example request.json file: 
```json
{
  "url": "/api/v3/shell-descriptors",
  "tenant": "TENANT_ONE",
  "method": "GET",
  "body": null
}
```

#### expected-response.json 
This file contains the JSON-formatted expected response from the system, which will be used to validate the system's output against what is expected.
The json has the following parameters:

| Field            | Description                         |
|------------------|-------------------------------------|
| `status`         | response code                       |
| `content`        | True if content needs to be compared|
| `assertions`     | Type of assertions                  |
| `expectedPayload`| Compare the payload                 |

Example expected-response.json file: 
```json
{
  "status": 200,
  "content": true,
  "assertions": [{
    "jsonPath": "$.result",
    "exists": true
  }
  ],
  "expectedPayload": null
}
```

Following assertions can be used for compare the response:

**exists:**
Check if path exists (true/false)
```json
{
  "jsonPath": "$.result",
  "exists": true
}
```

**equals:**
Check if path equals value:
```json
{
  "jsonPath": "$.messages[0].text",
  "equals": "Incorrect Base64 encoded value provided as parameter"
}
```

**hasSize:**
Check if a list has defined size:
```json
{
  "jsonPath": "$.submodelDescriptors",
  "hasSize": 0
}
```

**contains:**
Check if a path contains value:
```json
{
  "jsonPath": "$.messages[0].text",
  "contains": "value"
}
```

**doesNotExist:**
Check if a path does not exists:
```json
{
  "jsonPath": "$.messages[0].text",
  "doesNotExist": false
}
```

**hasItem:**
Check if a path has item:
```json
{
  "jsonPath": "$.submodelDescriptors[*].id",
  "hasItem": "574e43bc-5c14-449a-afec-bd82d029573f"
}
```

**isEmpty:**
Check if a path is empty:
```json
{
  "jsonPath": "$.paging_metadata",
  "isEmpty": true
}
```

**isNotEmpty:**
Check if a path is not empty:
```json
{
  "jsonPath": "$.paging_metadata",
  "isNotEmpty": false
}
```

## Add new tests
When adding tests, follow these steps to maintain consistency: 
1. Identify Use Cases: Start by identifying all the use cases that need to be tested. Each use case should correspond to a specific flow or feature of the application.
2. Define Actions: Break down each use case into discrete actions. Each action should represent a meaningful operation in the context of the use case.
3. Create JSON Files:
   * For each action, create a request.json file containing the payload to be sent to the system.
   * Define the expected output in an expected-response.json file for each action.
   * <span style="color:red;">!!! IMPORTANT: The **shellIds, submodeldescriptorIds, idShorts** must be unique. For every POST action create a random/unique UUID and idShort !!!</span>








