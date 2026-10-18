# PRODUCT SPECIFICATION & IMPLEMENTATION TASK

Spec: Walk 17 Oct modernization
Plan key: P1
Target repo: piggymetricsJava

## Current Task
Implement Walk 17 Oct modernization: every plan and todo below, in plan order.
Commit messages name the todo ids they complete (P<n>.T<m>).

- P1 Baseline and design: the target behavior, compatibility constraints, and module-level change plan are agreed
  - P1.T1 confirm the intended rework with the author and map module boundaries and dependencies from the root and service Maven manifests
- P2 In-place service rework: each affected module builds and its relevant tests pass
  - P2.T1 implement the agreed changes within the existing account, authentication, configuration, gateway, monitoring, notification, and registry modules and update their Maven manifests only as required
- P3 Deployment validation: the composed services start successfully, integration behavior is validated, and CI passes
  - P3.T1 exercise the reworked estate through the development and deployment Compose definitions and verify the existing Travis CI workflow

## Goal
Rework the Java services in piggymetricsJava in place.

## Summary
piggymetricsJava is 9 Maven modules on spring-boot-starter-parent 2.0.3.RELEASE and Java 1.8. Modernization on piggymetricsJava. The Brief names no Maven module, so every module is in scope until a person narrows it.

## Delivery mode
Solo Delivery. Work on one branch cut from the repository default branch. Commit there. Open one pull request back to the default branch when the plans under Current Task are done. The run harness creates the branch and the pull request.

## Context and Scope
In: gateway, auth-service, account-service, statistics-service, notification-service, and the four platform modules config, monitoring, registry, turbine-stream-service.
In: preserve the HTTP paths the gateway routes: /uaa/** to auth-service, /accounts/** to account-service, /statistics/** to statistics-service, /notifications/** to notification-service (config/src/main/resources/shared/gateway.yml).
Out: a destination or a rewrite. This is a modernization on piggymetricsJava.
Repositories: sandeep-chanda/piggymetricsJava

## Architectural Requirements
Eureka keep. Feign keep. Zuul keep. Spring Security OAuth keep. Hystrix keep. Spring Cloud Config keep. Turbine keep.
The same seven lines are also written as Replace. That contradiction is resolved in the plan decisions.

## MUST-reuse
Application classes, bootstrap.yml files, shared config yml, Feign clients, and the four controllers named in the intake spec stay on their current paths.

## Verification
Run the existing Maven verification from the root pom.xml and confirm the account, auth, config, gateway, monitoring, notification, and registry modules build and test successfully. Then start the estate with docker-compose.dev.yml and docker-compose.yml and perform smoke checks for service startup, discovery, and configuration.
