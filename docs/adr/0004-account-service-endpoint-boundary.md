# ADR 0004: Keep C1/C3/C4 on account-service; defer C2 cutover (D-005)

- Status: Accepted
- Date: 2026-11-13
- Spec: Modernize PiggyMetricsJava account service to Microservices architecture

## Context

The approved contract for account-service is four HTTP endpoints (C1–C4), one `accounts` Mongo collection, and two outbound Feign edges (auth-service, statistics-service). A future read-side split could move `GET /current` (C2) to an `account-query-service`, but that module is out of scope for this delivery.

## Decision

1. Retain on account-service:
   - C1 `GET /{name}`
   - C2 `GET /current` (until an out-of-scope cutover)
   - C3 `PUT /current`
   - C4 `POST /`
2. Keep exclusive ownership of the `accounts` collection on account-service (`Account` `@Document`).
3. Keep the auth-service edge on the C4 create path and the statistics-service edge on the C3 update path, using the URL-based Feign clients from ADR 0001.
4. Do **not** add an `account-query-service` module in this scoped delivery (D-005); keep `account-service/pom.xml` as the jar packaging shape template for any future sibling.

## Consequences

- Public HTTP paths and stored collection stay compatible with the existing UI and gateway routes.
- Future C2 move is recorded only; no new Maven module lands now.
- Acceptance is guarded by `SplitBoundaryAcceptanceTest` and the existing controller/service/repository tests.
