# Guideline to use AWS Cognito as Identity Provider

DTR can support Keycloak and AWS Cognito as an Identity Provider for all clients that want to access the registry.
Client must fetch a token and send it together with the request.
Cognito behaves somehow different than Keycloak so some code modifications were necessary to use AWS Cognito as an Identity Provider for DTR.

## Configuring AWS Cognito

The main difference is that Cognito is not able to attach roles to app clients directly if no user is used for authentication.
For authentication only client id and client secret is used.
This makes it necessary to use a different approach to control the access of the different app clients.

### Resource Servers and Scopes

First of all a Resource server must be configured within Cognito (User pool → Domain → Create resource server). Otherwise it is not possible to use custom scopes.

After that the following scopes must be created which correspond to the DTR roles:

- `view_digital_twin`
- `add_digital_twin`
- `delete_digital_twin`
- `update_digital_twin`
- `submodel_access_control`
- `read_access_rules`
- `write_access_rules`

If newer versions of DTR have new roles also those must be created as described above.

The final name of the scope is then the name of the resource server combined with the custom scope.
For example, if the resource server name is `dtwinreg` then the scope name for view access is `dtwinreg/view_digital_twin`.

### App Integration App Clients

At least two app clients should be created (Applications → App clients → Login pages):

- one with ony the view scope (`view_digital_twin`) attached
- one with all scopes attached for full access

For additional roles additional app clients can be created or roles can be attached to different clients.
It depends on the granularity of your access management.

The app clients have a client id and a client secret that can be used together with the scopes to fetch a token.
If somebody wants to request a token with the credentials of the "view app client" and with the scope `add_digital_twin` authentication on Cognito will fail, because the client has not attached that scope.
So it can ge guaranteed that the "view app client" has only view access.

## Configuring DTR

### Config File

The `idm` subelement has a new attribute called `identity-provider`.
Two values are allowed:

- `keycloak`: This switches DTR to the default behavior that is already known
- `cognito`: This switches DTR to use AWS Cognito as identity provider

Here is a snippet of an example DTR config file:

```yaml
registry:
    # This wildcard prefix is used to make specificAssetIds public vor everyone.
    # The default-value "PUBLIC_READABLE" is used by all catenaX participants.
    external-subject-id-wildcard-prefix: PUBLIC_READABLE
    external-subject-id-wildcard-allowed-types: manufacturerPartId,assetLifecyclePhase
    idm:
        identity-provider: cognito
        # is our BPN
        owning-tenant-id: <OWN BPN>
        # Keycloak
        # public-client-id: default-client
        # The claim name of the keycloak claims where the name of the client-id is the value
        # tenant-id-claim-name: azp

        # Cognito
        # if this ID comes then only scope <resource server>/view_digital_twin is allowed
        public-client-id: <view digital twin AWS Cognito app client ID>
        # if this ID comes then every existing scope is allowed
        internal-client-id: <edit AWS Cognito app client ID>
```

If `cognito` is set as an identity provider the following attributes must be set:

- `public-client-id`: This is the client id of the app client in Cognito that has the `view_digital_twin` scope attached
- `internal-client-id`: This is the client is of the app client in Cognito that has the edit scopes attached

### DTR Asset in EDC

To access the DTR a token must be fetched first. For the management API the token must be fetched before the API will be called.
The token must be then included as a Bearer token in the header (same as for Keycloak tokens).

To fetch a token a token form AWS Cognito there is a corresponding URL:

`https://xxxx.auth.eu-west-1.amazoncognito.com/oauth2/token`

where `xxxx` is the custom pool ID of the AWS Cognito and the AWS Region (here it is `eu-west-1`).

The body of the `POST` request must contain the following values with `x-www-form-urlencoded` encoding:

```http request
grant_type: "client_credentials"
scope: "dtwinreg/" + the DTR role (e.g. dtwinreg/view_digital_twin) Also multiple scopes are possible separated by whitespace (e.g. dtwinreg/view_digital_twin dtwinreg/submodel_access_control)
client_id: "the client id of the app client that has scopes above assigned"
client_secret: "the secret of the app client with the client id above"
```

With the following example a token will be returned for view only access to DTR:

```http request
grant_type: "client_credentials"
scope: "dtwinreg/view_digital_twin"
client_id: "the client id of the app client that has scopes above assigned"
client_secret: "the secret of the app client with the client id above"
```

When EDC will access the DTR this will be done normally via a special asset that contains necessary information also for fetching the token to access the DTR:

DTR Asset request example:

```json
{
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "cx-common": "https://w3id.org/catenax/ontology/common#",
        "cx-taxo": "https://w3id.org/catenax/taxonomy#",
        "dct": "http://purl.org/dc/terms/"
    },
    "@id": "digital-twin-registry",
    "properties": {
        "dct:type": {
            "@id": "cx-taxo:DigitalTwinRegistry"
        },
        "cx-common:version": "3.0",
        "type": "data.core.digitalTwinRegistry"
    },
    "privateProperties": {},
    "dataAddress": {
        "@type": "DataAddress",
        "type": "HttpData",
        "baseUrl": "<the DTR's base URL>",
        "proxyPath": "true",
        "proxyBody": "true",
        "proxyMethod": "true",
        "proxyQueryParams": "true",
        "oauth2:clientId": "<the client ID of the app client that has only view scope>",
        "oauth2:clientSecretKey": "<the name of the secret in the vault where the client secret of the client ID above is stored>",
        "oauth2:tokenUrl": "https://<pool ID>.amazoncognito.com/oauth2/token",
        "oauth2:scope": "<resource server>/view_digital_twin"
    }
}
```

The OAuth2 properties point to the AWS Cognito service including the ID, secret, and the scope.
