# Architecture decision records

ADRs for account-service modernization on this repository.

| ADR | Title |
| --- | --- |
| [0001](0001-retire-eureka-use-feign-urls.md) | Retire Eureka for account-service; resolve peers by Feign URL |
| [0002](0002-retire-config-server-service-owned-settings.md) | Retire Config Server client; own settings in application.yml |
| [0003](0003-account-service-independent-dockerfile.md) | Package account-service with a module Dockerfile |
| [0004](0004-account-service-endpoint-boundary.md) | Keep C1/C3/C4 on account-service; defer C2 cutover (D-005) |
