# PRODUCT SPECIFICATION & IMPLEMENTATION TASK

Spec: Walk 22 Oct fresh

## Current Task
Implement **Walk 22 Oct fresh**: every plan and todo below, in plan order.
Commit messages name the todo ids they complete (`P‹n›.T‹m›`).

- P1 · Platform line
  the parent and the train every module builds on
- Done when: every module builds on the new line
  - P1.T1 · Move the parent from spring-boot-starter-parent 2.0.3.RELEASE to Spring Boot 3.5.16 · Spring Cloud 2025.0.3 · Java 21 · pom.xml
  - P1.T2 · config · re-resolve 3 starters on the new line · config/pom.xml
  - P1.T3 · monitoring · re-resolve 4 starters on the new line · monitoring/pom.xml
  - P1.T4 · registry · re-resolve 4 starters on the new line · registry/pom.xml
  - P1.T5 · gateway · re-resolve 7 starters on the new line · gateway/pom.xml
  - P1.T6 · auth-service · re-resolve 12 starters on the new line · auth-service/pom.xml
  - P1.T7 · account-service · re-resolve 17 starters on the new line · account-service/pom.xml
  - P1.T8 · statistics-service · re-resolve 17 starters on the new line · statistics-service/pom.xml
  - P1.T9 · notification-service · re-resolve 17 starters on the new line · notification-service/pom.xml
  - P1.T10 · turbine-stream-service · re-resolve 6 starters on the new line · turbine-stream-service/pom.xml
- P2 · Zuul → the successor
  retire Zuul in place
- Depends on: P1
- Done when: no Zuul marker, starter or config key remains; the successor is in its place; every test that names it stays green
  - P2.T1 · Replace @EnableZuulProxy · gateway/src/main/java/com/piggymetrics/gateway/GatewayApplication.java:L10
  - P2.T2 · gateway · starter spring-cloud-starter-netflix-zuul → the successor · gateway/pom.xml
  - P2.T3 · gateway · @EnableZuulProxy → the successor · gateway/pom.xml
  - P2.T4 · Config key zuul.ignoredServices → the successor's key · config/src/main/resources/shared/gateway.yml:L14
  - P2.T5 · Config key zuul.host.connect-timeout-millis → the successor's key · config/src/main/resources/shared/gateway.yml:L16
  - P2.T6 · Config key zuul.host.socket-timeout-millis → the successor's key · config/src/main/resources/shared/gateway.yml:L17
  - P2.T7 · Config key zuul.routes.auth-service.path → the successor's key · config/src/main/resources/shared/gateway.yml:L21
  - P2.T8 · Config key zuul.routes.auth-service.url → the successor's key · config/src/main/resources/shared/gateway.yml:L22
  - P2.T9 · Config key zuul.routes.auth-service.stripPrefix → the successor's key · config/src/main/resources/shared/gateway.yml:L23
  - P2.T10 · Config key zuul.routes.account-service.path → the successor's key · config/src/main/resources/shared/gateway.yml:L27
  - P2.T11 · Config key zuul.routes.account-service.serviceId → the successor's key · config/src/main/resources/shared/gateway.yml:L28
  - P2.T12 · Config key zuul.routes.account-service.stripPrefix → the successor's key · config/src/main/resources/shared/gateway.yml:L29
  - P2.T13 · Config key zuul.routes.statistics-service.path → the successor's key · config/src/main/resources/shared/gateway.yml:L33
  - P2.T14 · Config key zuul.routes.statistics-service.serviceId → the successor's key · config/src/main/resources/shared/gateway.yml:L34
  - P2.T15 · Config key zuul.routes.statistics-service.stripPrefix → the successor's key · config/src/main/resources/shared/gateway.yml:L35
  - P2.T16 · Config key zuul.routes.notification-service.path → the successor's key · config/src/main/resources/shared/gateway.yml:L39
  - P2.T17 · Config key zuul.routes.notification-service.serviceId → the successor's key · config/src/main/resources/shared/gateway.yml:L40
  - P2.T18 · Config key zuul.routes.notification-service.stripPrefix → the successor's key · config/src/main/resources/shared/gateway.yml:L41
  - P2.T19 · Keep test StatisticsServiceClientFallbackTest green
- P3 · Hystrix → Resilience4j
  retire Hystrix in place
- Depends on: P1
- Done when: no Hystrix marker, starter or config key remains; Resilience4j is in its place; every test that names it stays green
  - P3.T1 · Replace @EnableCircuitBreaker · account-service/src/main/java/com/piggymetrics/account/AccountApplication.java:L15
  - P3.T2 · Replace feign.hystrix.enabled · config/src/main/resources/shared/account-service.yml:L26
  - P3.T3 · Replace hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds · config/src/main/resources/shared/application.yml:L11
  - P3.T4 · Replace hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds · config/src/main/resources/shared/gateway.yml:L7
  - P3.T5 · Replace @EnableHystrixDashboard · monitoring/src/main/java/com/piggymetrics/monitoring/MonitoringApplication.java:L8
  - P3.T6 · monitoring · starter spring-cloud-starter-netflix-hystrix-dashboard → Resilience4j · monitoring/pom.xml
  - P3.T7 · monitoring · @EnableHystrixDashboard → Resilience4j · monitoring/pom.xml
  - P3.T8 · Config key hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds → the successor's key · config/src/main/resources/shared/gateway.yml:L7
  - P3.T9 · account-service · starter spring-cloud-starter-netflix-hystrix → Resilience4j · account-service/pom.xml
  - P3.T10 · account-service · starter spring-cloud-netflix-hystrix-stream → Resilience4j · account-service/pom.xml
  - P3.T11 · Config key feign.hystrix.enabled → the successor's key · config/src/main/resources/shared/account-service.yml:L26
  - P3.T12 · Keep StatisticsServiceClientFallback as the fallback under the successor breaker · account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java
  - P3.T13 · statistics-service · starter spring-cloud-netflix-hystrix-stream → Resilience4j · statistics-service/pom.xml
  - P3.T14 · Keep ExchangeRatesClientFallback as the fallback under the successor breaker · statistics-service/src/main/java/com/piggymetrics/statistics/client/ExchangeRatesClient.java
  - P3.T15 · notification-service · starter spring-cloud-netflix-hystrix-stream → Resilience4j · notification-service/pom.xml
  - P3.T16 · Keep test StatisticsServiceClientFallbackTest green
- P4 · Ribbon → Spring Cloud LoadBalancer
  retire Ribbon in place
- Depends on: P1
- Done when: no Ribbon marker, starter or config key remains; Spring Cloud LoadBalancer is in its place; every test that names it stays green
  - P4.T1 · Replace ribbon.ReadTimeout · config/src/main/resources/shared/gateway.yml:L10
  - P4.T2 · Replace ribbon.ConnectTimeout · config/src/main/resources/shared/gateway.yml:L11
  - P4.T3 · Config key ribbon.ReadTimeout → the successor's key · config/src/main/resources/shared/gateway.yml:L10
  - P4.T4 · Config key ribbon.ConnectTimeout → the successor's key · config/src/main/resources/shared/gateway.yml:L11
  - P4.T5 · Keep test StatisticsServiceClientFallbackTest green
- P5 · Consumers of the retired components
  what read the streams that stop
- Depends on: P2, P3, P4
- Done when: each consumer has its decision recorded
  - P5.T1 · monitoring · reads the Hystrix stream this scenario retires (@EnableHystrixDashboard)
  - P5.T2 · turbine-stream-service · reads the Hystrix stream this scenario retires (@EnableTurbineStream)

## Goal
- Upgrade piggymetricsJava in place to Spring Boot 3 and Java 21. Keep the same repo.

## Summary
- piggymetricsJava is 9 Maven modules (account-service, auth-service, config, gateway, monitoring, notification-service, registry, statistics-service, turbine-stream-service) · 97 Java files on spring-boot-starter-parent 2.0.3.RELEASE · Java 1.8. 5 runtime services carry behaviour (gateway, auth-service, account-service, statistics-service, notification-service); 4 infrastructure modules exist for the platform (config (config-server), monitoring (dashboard), registry (registry), turbine-stream-service (metrics-stream)) and become platform decisions, not units. The read states 12 HTTP endpoints, 4 stored collections, 4 outbound clients, 2 scheduled jobs, 4 OAuth clients and 61 tests in 25 classes.
