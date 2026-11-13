# ADR 0003: Package account-service with a module Dockerfile

- Status: Accepted
- Date: 2026-11-13
- Spec: Modernize PiggyMetricsJava account service to Microservices architecture

## Context

With Eureka and Config retired for account-service, the module can run from its own jar and environment without depending on repository-level compose or deployment manifests for image definition.

## Decision

1. Define `account-service/Dockerfile` on `eclipse-temurin:17-jre`.
2. Package the runnable jar produced by `account-service/pom.xml` (`target/account-service.jar`) and launch `AccountApplication`.
3. Expose port 6000; require runtime env for MongoDB, OAuth client credentials, and peer URLs — not Eureka or Config.

## Consequences

- Image builds are scoped to the account-service module after its tests pass.
- Compose and Travis workflows elsewhere in the repo are unchanged by this ADR; the Dockerfile stands alone for independent packaging.
