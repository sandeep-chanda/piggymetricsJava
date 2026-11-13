# account-service API

Context path: `/accounts` (port `6000`). Gateway prefix: `/accounts/**`.

Public HTTP surface is unchanged by the Eureka/Config retirement. Outbound peers now use URL properties instead of Eureka. See [ADR 0001](../adr/0001-retire-eureka-use-feign-urls.md), [ADR 0002](../adr/0002-retire-config-server-service-owned-settings.md), and [ADR 0004](../adr/0004-account-service-endpoint-boundary.md).

## Endpoints

| Id | Method | Service path | Gateway path | Auth | Success body |
| --- | --- | --- | --- | --- | --- |
| C1 | GET | `/{name}` | `/accounts/{name}` | scope `server`, or `name` equals `demo` | `Account` |
| C2 | GET | `/current` | `/accounts/current` | authenticated principal | `Account` |
| C3 | PUT | `/current` | `/accounts/current` | authenticated principal; validated `Account` body | empty |
| C4 | POST | `/` | `/accounts/` | anonymous permitted; validated `User` body | `Account` |

- C2 remains on account-service until an out-of-scope `account-query-service` cutover; C1, C3, and C4 stay here.
- Demo account is reachable as C1 with `name=demo` (UI also lists `/accounts/demo`).

Controller: `account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java`.

## Data

Collection `accounts` owned by account-service. Document type `Account`:

- `_id` / `name`: string
- `lastSeen`: date (server managed)
- `incomes` / `expenses`: `Item[]` (`title`, `amount`, `currency`, `period`, `icon`), `@Valid`
- `saving`: `Saving` (`amount`, `currency`, `interest`, `deposit`, `capitalization`), `@Valid` `@NotNull`
- `note`: string, length 0…20_000

## Outbound edges

| Edge | Client | Call | Failure behaviour | Base URL property |
| --- | --- | --- | --- | --- |
| E1 | `AuthServiceClient` | `POST /uaa/users` | propagates | `auth-service.url` ← `AUTH_SERVICE_URL` (default `http://auth-service:5000`) |
| E2 | `StatisticsServiceClient` | `PUT /statistics/{accountName}` | `StatisticsServiceClientFallback` | `statistics-service.url` ← `STATISTICS_SERVICE_URL` (default `http://statistics-service:7000`) |

OAuth client credentials and user-info URI remain service-owned (`ACCOUNT_SERVICE_PASSWORD`, `AUTH_SERVICE_TOKEN_URI`, `AUTH_SERVICE_USER_INFO_URI`).

## Retired platform edges (this module)

- E3 Config Server (`http://config:8888`, fail-fast) — removed; settings live in `application.yml`.
- E4 Eureka `@EnableDiscoveryClient` — removed; peers use Feign URLs.

## Packaging

- Artifact: `account-service.jar` (`finalName` account-service), produced after module tests.
- Image: `account-service/Dockerfile` on Java 17 JRE; launch does not require Eureka or Config.
