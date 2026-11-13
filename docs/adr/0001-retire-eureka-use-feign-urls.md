# ADR 0001: Retire Eureka for account-service; resolve peers by Feign URL

- Status: Accepted
- Date: 2026-11-13
- Spec: Modernize PiggyMetricsJava account service to Microservices architecture

## Context

account-service used Netflix Eureka (`@EnableDiscoveryClient`, `spring-cloud-starter-netflix-eureka-client`) so OpenFeign clients could resolve `auth-service` and `statistics-service` by logical name. The modernization scope is limited to account-service and retires Eureka for this module while keeping Feign, OAuth2 client enablement, Hystrix, and method security.

## Decision

1. Remove `@EnableDiscoveryClient` and the Eureka client starter from account-service.
2. Keep `@EnableFeignClients`, `@EnableOAuth2Client`, `@EnableCircuitBreaker`, and `@EnableGlobalMethodSecurity`.
3. Declare Feign peers with explicit URL properties:
   - `AuthServiceClient` → `@FeignClient(name = "auth-service", url = "${auth-service.url}")`, still `POST /uaa/users` with OAuth propagation.
   - `StatisticsServiceClient` → `@FeignClient(name = "statistics-service", url = "${statistics-service.url}", fallback = StatisticsServiceClientFallback.class)`, still `PUT /statistics/{accountName}` with the existing Hystrix fallback.
4. Source Feign / Hystrix timeout defaults remain in service-owned settings.

## Consequences

- account-service starts and calls peers without a registry.
- Deployments must set `AUTH_SERVICE_URL` / `STATISTICS_SERVICE_URL` (or the YAML defaults) to reachable bases.
- Other modules may continue to use Eureka; this ADR applies only to account-service.
