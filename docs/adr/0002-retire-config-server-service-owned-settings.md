# ADR 0002: Retire Config Server client; own settings in application.yml

- Status: Accepted
- Date: 2026-11-13
- Spec: Modernize PiggyMetricsJava account service to Microservices architecture

## Context

account-service previously required Spring Cloud Config at startup (`spring.cloud.config.uri: http://config:8888` with `fail-fast: true` in `bootstrap.yml`). Unreachable Config blocked boot (edge E3 / risk R3). After retiring Eureka name resolution, the module also needs local ownership of the Feign URL properties and remaining runtime settings.

## Decision

1. Remove the mandatory shared Config-server block from `account-service/src/main/resources/bootstrap.yml`, retaining only the local `spring.application.name: account-service` identity.
2. Remove `spring-cloud-starter-config` from `account-service/pom.xml`.
3. Add service-owned `account-service/src/main/resources/application.yml` with application name, MongoDB/RabbitMQ/OAuth settings, `auth-service.url`, `statistics-service.url`, and Feign/Hystrix defaults, all overridable by environment for deployment-specific values.
4. Keep the existing Spring Boot `finalName` `account-service` and test-before-jar lifecycle so `account-service.jar` is produced only after module tests pass.

## Consequences

- account-service no longer fails fast when Config is down.
- Shared `config/` module settings for account-service are no longer the runtime source of truth for this module.
- Operators override via env (`MONGODB_*`, `ACCOUNT_SERVICE_PASSWORD`, `AUTH_SERVICE_URL`, `STATISTICS_SERVICE_URL`, token/user-info URIs, and related keys).
