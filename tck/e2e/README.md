# E2E test

An end-to-end test of the Digital Twin Registry in a running dataspace. It uses the registry
the way a partner does, through the owner's connector, and checks that the promise holds.

| Test | Kind | What it proves |
| --- | --- | --- |
| `register_and_read` | Positive, contract | A twin its owner registers can be read back, in the shape the JSON schema in `schemas/` expects. |
| `partner_reads_through_connector` | Positive, the journey | A partner negotiates access through the owner's connector, finds the twin by its part number and reads it. |
| `unknown_twin_is_not_found` | Negative | A twin the registry never held is answered with 404. |

The project is a TCK project for [Eclipse Tractus-X TestLab](https://github.com/eclipse-tractusx/tractusx-testlab):
YAML only, no test code. Everything a test creates is removed again in its teardown.

## In CI

[`.github/workflows/e2e-test.yml`](../../.github/workflows/e2e-test.yml) validates and compiles
the project on every pull request and runs it in the Eclipse Tractus-X developer toolbox, a
small dataspace created inside the GitHub runner (two connectors, two wallets, this registry,
a BDRS and an IssuerService). It needs no secrets.

## On your machine

With Python 3.12 or newer:

```bash
pip install "tractusx-testlab==1.0.0a24"
cd tck/e2e
testlab validate index.yaml
testlab compile index.yaml -o /tmp/dtr-e2e.tck
testlab inspect /tmp/dtr-e2e.tck --variables --infrastructure
```

To run it you need two connectors and a registry; the workflow creates them for you.

## Learn more

This test is the worked example of the tutorial
[E2E Tests in Eclipse Tractus-X](https://eclipse-tractusx.github.io/tutorial-resources/e2e-testing/),
which explains how it was built and how to write one for another product.

## NOTICE

This work is licensed under the [CC-BY-4.0](https://creativecommons.org/licenses/by/4.0/legalcode).

- SPDX-License-Identifier: CC-BY-4.0
- SPDX-FileCopyrightText: 2026 Contributors to the Eclipse Foundation
- Source URL: <https://github.com/eclipse-tractusx/sldt-digital-twin-registry>
