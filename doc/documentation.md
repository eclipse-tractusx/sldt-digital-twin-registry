# Developer Documentation Tractus-X Digital Twin Registry
This page provides an overview of the Digital Twin Registry and all relevant information for developers to get started with integration against the Digital Twin Registry.

## Architectural Overview
The Digital Twin Registry acts as an address book for Digital Twins. Data Providers register their Digital Twins in the Digital Twin Registry. Data consumers query the Digital Twin Registry to find Digital Twins and interact with them further. A Digital Twin contains endpoint references to Submodel endpoints. Calling a Submodel endpoint returns data compliant to a semantic model. A semantic model describes the data that a Submodel endpoint returns.
![](img/image009.png)


## Asset Administration Shell Domain Model
The Asset Administration Shell Registry is an address book for Asset Administration Shell Descriptors. The diagram below, shows the domain model of the Asset Administration Shell Registry (AAS Registry).
![](img/image001.png)

The following table shows the synonyms for each of the domain objects above.

|Digital Twin Registry |  Asset Administration Shell Registry |
|---|---|
| DigitalTwin  | AssetAdministrationShellDescriptor  |
| Aspect  | SubmodelDescriptor  |
| LocalIdentifiers  | SpecificAssetIds  |


For the purpose of simplification the diagram above does only show the required fields. Below is the complete Asset Administration Shell Descriptor payload in JSON.

```
{
  "description": [
    {
      "language": "en",
      "text": "The shell for a vehicle"
    }
  ],
  "globalAssetId": {
    "value": [
      "c02729fe-915e-416c-9723-f2781627f3e2"
    ]
  },
  "idShort": "future concept x",
  "identification": "882fc530-b69b-4707-95f6-5dbc5e9baaa8",
  "specificAssetIds": [
    {
      "key": "PartID",
      "value": "12309481209312"
    }
  ],
  "submodelDescriptors": [
    {
      "identification": "urn:uuid:53125dc3-5e6f-4f4b-838d-447432b97918",
      "idShort": "serialPartTypization",
      "semanticId": {
        "value": [
          "urn:bamm:com.catenax.serial_part_typization:1.0.0"
        ]
      },
      "endpoints": [
        {
          "interface": "EDC",
          "protocolInformation": {
            "endpointAddress": "edc://provider.connector:port/BPNL7588787849VQ/urn%3Auuid%3Ac227a880-b82b-40f7-846c-3942ddf26c29-urn%3Auuid%3A53125dc3-5e6f-4f4b-838d-447432b97918/submodel?content=value&extent=WithBLOBValue",
            "endpointProtocol": "IDS/ECLIPSE DATASPACE CONNECTOR",
            "endpointProtocolVersion": "0.0.1-SNAPSHOT"
          }
        }
      ]
    }
  ]
}
```

## Actors and interaction diagrams
There are two actors who interact with the AAS Registry.

|Actor |  	Description | Examples |
|---|---|---|
| Data provider  | The data provider registers AAS Descriptors and Submodel Descriptors so that consumers can query for AAS Descriptors and request data via the Submodel Descriptor Endpoints. Responsibilities: Providing EDC compatible Submodel Descriptor Endpoints, Registration of the AAS Descriptors  | Any manufacturer who provides data for their assets |
| Data consumer  | 	The data consumers are accessing the AAS Registry to discover and consume data from the Submodel Descriptor Endpoints. Responsibilities: Query the AAS Registry for AAS Descriptors, Access the Submodel Descriptor Endpoints via EDC  |  |

The interactions of both actors are shown in the diagrams below.
For the purpose of simplifying, the interactions via EDC is not shown.

EDC is involved as following:
1. Interactions with the AAS Registry must not be done with EDC
2. Interactions with Submodel Endpoints (Data Provider) have to be done with EDC

## Data provider
To be able to register a DigitalTwin the following prerequisites must be met.
1. The identifiers for an asset are known (specificAssetIds, e.g. serial number, part id)
2. An endpoint that provides the data for the asset is available. The data has to be complaint with an Aspect Model.

### Without EDC (simplified)
![](img/image002.png)

### With EDC
![](img/image003.png)


## Data consumer

![](img/image004.png)



## Uniqueness
The following table contains the identifier fields and wether they are globally unique, unique for an
AAS Descriptor or not unique at all.

| Field | Unique globally | Explanation |
|---|---|---|
| AssetAdministrationShellDescriptor#identification  | yes  | - |
| AssetAdministrationShellDescriptor#specificAssetIds#key	  | -  | The specificAssetIds are primarily used for searches. There are use cases where multiple values for the same key can exist. For instance, an OEM can have multiple World Manufacturer Identifier (WMI). Queries against any of the WMI must be possible as data consumers may know only one of the WMI.|
|  AssetAdministrationShellDescriptor#submodelDescriptor#identification  | yes  | - |


Uniqueness for natural keys e. g. serial numbers is not given in the manufacturing world.
Therefore the AAS Registry cannot enforce uniqueness for natural keys (specificAssetIds). 

## Identifiers

### Identification and globalAssetId

| Field | Value | Description |
|---|---|---|
| AssetAdministrationShellDescriptor#identification  | Random UUIDv4  | - |
| AssetAdministrationShellDescriptor#globalAssetId[0]  | Random UUIDv4  | The globalAssetId is a natural key that identifies an asset uniquely. An example for instance is the MAC - Address. In the manufacturing industry not all assets do have a global unique natural key. It was decided therefore to use a random UUID. |
| SubmodelDescriptor#identification  | Random UUIDv4  | - |


### Predefined specificAssetIds
The `specificAssetIds` are collection key-value pairs and the fundamental part for discovery capabilities of the AAS Registry. Data consumers use these specificAssetIds to find AAS Descriptors.
In multiple discussions with different Product Owners and architects, the keys for `specificAssetIds` were defined.
The AAS Registry does not enforce any `specificAssetId key`. However, data providers should use one of these keys if the Asset Administration Shell Descriptor does match one of the defined types e.g. Serial Part, Vehicle.
If you do not find a matching `specificAssetId` key for your use case please contact us so that we can extend the list.

## Submodel Descriptor Endpoints
   The Submodel Descriptor contains all the required information to obtain data from a remote address.
   The endpoint for a Submodel Descriptor must be setup as follows:

| Property | Description | Example Value | 
|---|---|---|
| interface  | The type of the endpoint. Only "EDC" is currently supported.  | EDC |
| semanticId  |	The urn of the Semantic Model that describes the data of this endpoint.  | urn:bamm:com.catenax:1.0.0#ExampleModel |
| protocolInformation#endpointAddress  | The EDC compatible endpoint where the semanticId compatible data can be fetched.  Catena-X  defined the URL structure as: Read Endpoint: http://<HOST>/<BPN>/<AAS_ID>-<SUBMODEL_ID>/submodel?content=value Operations Endpoint: <SAME_AS_READ/<operationName>/invoke?content=value | Read Endpoint: http://myawesomeconnector.com/BPNL7588787849VQ/urn%3Auuid%3Ac227a880-b82b-40f7-846c-3942ddf26c29-urn%3Auuid%3A53125dc3-5e6f-4f4b-838d-447432b97918/submodel?content=value&extent=WithBLOBValue
Operation Endpoint: http://myawesomeconnector.com/BPNL7588787849VQ/urn%3Auuid%3Ac227a880-b82b-40f7-846c-3942ddf26c29-urn%3Auuid%3A53125dc3-5e6f-4f4b-838d-447432b97918/submodel/vin-van-converter/invoke?content=value |
| protocolInformation#endpointProtocol  |  The protocol of the endpoint. | HTTPS |
|  protocolInformation#endpointProtocolVersion |  The version of the protocol. |  1.0|

## Authentication & Authorization
   The AAS Registry needs to be integrated with an OAuth2 compliant authorization server. Every API call has to provide a valid Bearer Token issued by this authorization server.
   Authorization is supported by Role Based Access Control (RBAC). Following roles are available:

| Role | 	Description | 
|---|---|
| view_digital_twin | Can read all digital twins.|
|  add_digital_twin | Can add a digital twin.|
|  update_digital_twin |Can update a digital twin.Users can only update digital twins they own. Ownership is ensured by the tenantId/BPN of the user. |
| delete_digital_twin |Can delete a digital twin. Users can only delete digital twins they own. Ownership is ensured by the tenantId/BPN of the user.|
   
The Swagger UI of the AAS Registry is integrated with IAM. You can check the Swagger UI API calls for examples.

### Visibility of specificAssetIds based on tenantId/BPN
You can control the visibility of specificAssetIds based on the tenantId/BPN.
- You can provide the tenantId/BPN as attribute to a specificAssetId. Only users having the same tenantId/BPN in the access token are able to see the specificAssetId.
- The specificAssetIds of Digital Twins you created will always be shown to you.

Detailed example:
```
// Given specificAssetIds:
[
  {
    "key": "CustomerPartId",
    "value": "293913",
    "externalSubjectId": {
      "value": [
        "BPN12"
      ]
    }
  },
  {
    "key": "CustomerPartId",
    "value": "429212",
    "externalSubjectId": {
      "value": [
        "BPN49"
      ]
    }
  },
  {
    "key": "CustomerPartId",
    "value": "523192",
    "externalSubjectId": {
      "value": [
        "BPN29"
      ]
    }
  },
  {
    "key": "MaterialNumber",
    "value": "39192"
  }
]
// A customer with (BPN12) will only get the specificAssetIds that contains his BPN/tenantId. Taking the above example, the response for the customer //(BPN12) would be:
[
  {
    "key": "CustomerPartId",
    "value": "293913",
    "externalSubjectId": {
      "value": [
        "BPN12"
      ]
    }
  },
  {
    "key": "MaterialNumber",
    "value": "39192"
  }
]
// Lookup API:  GET /shells/lookup and POST /shells/lookup/query with BPN12
// REQUEST:
[
  {
    "key": "CustomerPartId",
    "value": "429212" // note: the externalSubjectId of this assetId is BPN49
  }
]
// RESPONSE:
// The response is empty, because the above assetId belongs to customer (BPN49) and not to the  customer (BPN12).
[]
```

### Authentication on behalf of a user
The AAS Registry can be accessed on behalf of a user. The token has to be obtained via the OpenID Connect flow. The AAS Registry will validate these tokens.

#### Postman configuration
![](img/image005.png)

*Support contact*	tractusx-dev@eclipse.org


## Remarks
The Digital Twin Registry implementation is not 100 % specification compliant. Please find the current deviations in the table below.

| No | Deviation |  Reason |
|---|---|---|
| 1. | The field AssetAdministrationShellDescriptor#endpoints |  is optional in the AAS Registry implementation and will be ignored. |
| 2. | The response of the GET /registry/shell-descriptors was changed to support pagination. | Addressed as feedback to the AAS Specification |
| 3. | The POST /registry/shell-descriptors/batch endpoint was introduced to support the creation of AAS Descriptors in batch | Addressed as feedback to the AAS Specification |
| 4. | The specificAssetIds#SubjectIds field is optional and not supported. The AAS Registry ignores this field. |  |
| 5. | The POST /lookup/shells/query endpoint was introduced to support advanced query capabilities that are not yet defined in the specification | - |
