# PRODUCT SPECIFICATION & IMPLEMENTATION TASK

Spec: walk-e2e-20261024-scenario

## Current Task
Implement **walk-e2e-20261024-scenario**: every plan and todo below, in plan order.
Commit messages name the todo ids they complete (`P‹n›.T‹m›`).

- P1 · Extend account-service
  enhancement in account-service
- Done when: the behaviour holds in the module's tests
  - P1.T1 · Endpoints the change reaches · GET /accounts/{name}, GET /accounts/current, PUT /accounts/current, POST /accounts · account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java
  - P1.T2 · Documents the change reads or extends · Account (accounts) · account-service/src/main/java/com/piggymetrics/account/domain/Account.java
  - P1.T3 · Repository queries · account-service/src/main/java/com/piggymetrics/account/repository/AccountRepository.java
  - P1.T4 · Clients the change calls · POST /uaa/users on auth-service · on failure: propagates · account-service/src/main/java/com/piggymetrics/account/client/AuthServiceClient.java
  - P1.T5 · Clients the change calls · PUT /statistics/{accountName} on statistics-service · on failure: fallback · StatisticsServiceClientFallback · account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java
  - P1.T6 · New keys beside the module's own · config/src/main/resources/shared/account-service.yml
  - P1.T7 · Extend test AccountServiceApplicationTests
  - P1.T8 · Extend test StatisticsServiceClientFallbackTest
  - P1.T9 · Extend test AccountControllerTest
  - P1.T10 · Extend test AccountRepositoryTest
  - P1.T11 · Extend test AccountServiceTest

## Goal
- Add settlement latency and failed-batch observability to the account-service and statistics-service: 1) expose a p95 settlement latency metric, 2) add a failed-batch counter with retry, 3) add a dashboard endpoint that reads both, 4) add tests for each. Item 3 depends on 1 and 2; item 4 depends on 1-3.
