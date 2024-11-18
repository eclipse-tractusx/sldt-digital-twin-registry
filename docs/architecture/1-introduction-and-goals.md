## 1 Introduction and goals
The Digital Twin Registry (DTR) acts as an address book for Digital Twins. Data Providers register their Digital Twins in the Digital Twin Registry. Data consumers query the Digital Twin Registry to find Digital Twins and interact with them further. A Digital Twin contains endpoint references to Submodel endpoints. Calling a Submodel endpoint returns data compliant to a semantic model. A semantic model describes the data that a Submodel endpoint returns.
The Digital Twin Registry is deployed as a decentralized component. That means that every Data Provider runs its own Digital Twin Registry.

### High level requirement
The DTR acts as an address book for finding Digital Twins.

### Quality goals

- Ensure that Digital Twins can be found.
- Data sovereignty is given. The provider is responsible for his data.
- All users and services are secured and can only access when authenticated and authorized.

### Stakeholders

| Role          | Description                                                  | Goal, Intention                           |
|---------------|--------------------------------------------------------------|-------------------------------------------|
| Data Consumer | uses the DTR                                                 | wants to find endpoints for Digital Twins |
| Data Provider | runs its own DTR and provides endpoints to his Digital Twins | wants to provide his twins                |

### NOTICE

This work is licensed under the [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0).

- SPDX-License-Identifier: Apache-2.0
- SPDX-FileCopyrightText: 2024 Robert Bosch Manufacturing Solutions GmbH
- SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
- Source URL: https://github.com/eclipse-tractusx/sldt-digital-twin-registry.git