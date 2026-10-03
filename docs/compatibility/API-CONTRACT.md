# PiggyMetrics public API compatibility contract

- Status: proposed. Approval of this file is the exit condition of plan P1 of spec "Fresh data".
- Baseline: `sandeep-chanda/piggymetricsJava` at `f760b95a538cfc199b1ce8d7893e55cd7a7c22ac` (2026-09-26T20:08:58Z).
- Machine twin: `baseline.json`. Checks: `ACCEPTANCE-CHECKS.md`. Open items: `CONFIRMATIONS.md`.

Observed behaviour only, read from the baseline sources cited below. Nothing here is aspirational.
A reworked service is compatible when it reproduces every row without exception.

## 1. Gateway routing

Zuul proxy on container port 4000, published on host port 80. Four prefixes and nothing else
(`zuul.ignoredServices: '*'`). Every route sets `stripPrefix: false`, so each service receives the
full incoming path and matches it against its own servlet context path. Connect and socket
timeouts are 20000 ms. Source: `config/src/main/resources/shared/gateway.yml`.

| Prefix | Service | Context path | Port | Route style |
| --- | --- | --- | --- | --- |
| /uaa/** | auth-service | /uaa | 5000 | static url http://auth-service:5000 |
| /accounts/** | account-service | /accounts | 6000 | serviceId account-service |
| /statistics/** | statistics-service | /statistics | 7000 | serviceId statistics-service |
| /notifications/** | notification-service | /notifications | 8000 | serviceId notification-service |

- G-1 those four prefixes are the entire externally reachable surface.
- G-2 a path that reaches the gateway is forwarded unmodified; no prefix is stripped.
- G-3 `sensitiveHeaders` is blank on all four routes, so `Authorization` is forwarded.

## 2. Endpoint inventory (12)

| Id | Method | Gateway path | Auth rule | Success | Success body |
| --- | --- | --- | --- | --- | --- |
| AUTH-01 | POST | /uaa/oauth/token | HTTP Basic client credentials | 200 | TokenResponse |
| AUTH-02 | GET | /uaa/users/current | Bearer, any authenticated principal | 200 | PrincipalResponse |
| AUTH-03 | POST | /uaa/users | Bearer with scope `server` | 200 | empty |
| ACCT-01 | POST | /accounts/ | anonymous permitted | 200 | Account |
| ACCT-02 | GET | /accounts/{name} | scope `server`, or `name` equals `demo` | 200 | Account |
| ACCT-03 | GET | /accounts/current | Bearer, any authenticated principal | 200 | Account |
| ACCT-04 | PUT | /accounts/current | Bearer, any authenticated principal | 200 | empty |
| STAT-01 | GET | /statistics/current | Bearer, any authenticated principal | 200 | DataPoint[] |
| STAT-02 | GET | /statistics/{accountName} | authenticated and (scope `server` or `accountName` equals `demo`) | 200 | DataPoint[] |
| STAT-03 | PUT | /statistics/{accountName} | Bearer with scope `server` | 200 | empty |
| NOTI-01 | GET | /notifications/recipients/current | Bearer, any authenticated principal | 200 | Recipient |
| NOTI-02 | PUT | /notifications/recipients/current | Bearer, any authenticated principal | 200 | Recipient |

Source: the four controllers under `*/src/main/java/com/piggymetrics/*/controller/` -
AccountController, UserController (class mapping `/users`), StatisticsController and
RecipientController (class mapping `/recipients`); AUTH-01 from
`auth-service/.../config/OAuth2AuthorizationConfig.java` with `@EnableAuthorizationServer`;
context paths from the four shared configuration files under `config/src/main/resources/shared/`.

- E-1 the inventory is exactly twelve rows. A thirteenth public route is a contract break.
- E-2 a path literal beats a path variable. `/accounts/current` resolves to ACCT-03 and never to ACCT-02 with `name` bound to `current`. Same for `/statistics/current` against STAT-02.
- E-3 ACCT-01 is mapped on the trailing-slash path. Its gateway path is `/accounts/` exactly.

## 3. Authorisation model

| Service | Role | Enforcement |
| --- | --- | --- |
| auth-service | authorization server | `WebSecurityConfig`: `anyRequest().authenticated()`, CSRF disabled, BCrypt user passwords |
| account-service | resource server | `ResourceServerConfig`: `antMatchers("/", "/demo").permitAll()`, then `anyRequest().authenticated()` |
| statistics-service | resource server | `@EnableResourceServer` default: every request authenticated |
| notification-service | resource server | `@EnableResourceServer` default: every request authenticated |

Resource servers resolve a bearer token against `http://auth-service:5000/uaa/users/current`
(`security.oauth2.resource.user-info-uri` in `shared/application.yml`).

Registered OAuth clients, from `OAuth2AuthorizationConfig.configure(ClientDetailsServiceConfigurer)`:

| Client id | Secret source | Grant types | Scope |
| --- | --- | --- | --- |
| browser | none | refresh_token, password | ui |
| account-service | env `ACCOUNT_SERVICE_PASSWORD` | client_credentials, refresh_token | server |
| statistics-service | env `STATISTICS_SERVICE_PASSWORD` | client_credentials, refresh_token | server |
| notification-service | env `NOTIFICATION_SERVICE_PASSWORD` | client_credentials, refresh_token | server |

Token store is `InMemoryTokenStore`; client secrets compare through `NoOpPasswordEncoder`;
`tokenKeyAccess` is `permitAll()`; `checkTokenAccess` is `isAuthenticated()`.

- AU-1 a user-facing call carries a `browser` token with scope `ui`.
- AU-2 a service-to-service call carries a `client_credentials` token with scope `server`.
- AU-3 those four client ids are the complete client registry.

## 4. Payload schemas

All bodies are `application/json`; field names are wire names and are case sensitive. `R` marks a
field the baseline validates as required on write.

- **Account**: `name` string (Mongo `_id`), `lastSeen` date-time (server managed), `incomes` AccountItem[], `expenses` AccountItem[], `saving` Saving R, `note` string.
- **AccountItem**: `title` string R, `amount` number R, `currency` enum R (USD EUR RUB), `period` enum R (YEAR QUARTER MONTH DAY HOUR), `icon` string R.
- **Saving** (account-service and statistics-service): `amount` number R, `currency` enum R, `interest` number R, `deposit` boolean R, `capitalization` boolean R.
- **StatisticsAccount** (request body of STAT-03): `incomes` StatisticsItem[] R, `expenses` StatisticsItem[] R, `saving` Saving R.
- **StatisticsItem**: `title`, `amount`, `currency`, `period`, all R. It carries no `icon`, the one shape difference from AccountItem.
- **DataPoint** (element of STAT-01 and STAT-02 arrays): `id.account` string, `id.date` date-time at start of day in the server zone, `incomes` ItemMetric[], `expenses` ItemMetric[], `statistics` object keyed INCOMES_AMOUNT EXPENSES_AMOUNT SAVING_AMOUNT, `rates` object keyed USD EUR RUB.
- **ItemMetric**: `title` string, `amount` number.
- **Recipient** (NOTI-01 and NOTI-02): `accountName` string (Mongo `_id`, bound from the principal), `email` string R, `scheduledNotifications` object keyed BACKUP and REMIND with NotificationSettings values.
- **NotificationSettings**: `active` boolean R, `frequency` enum R (WEEKLY MONTHLY QUARTERLY), `lastNotified` date-time (server managed).
- **RegistrationUser** (ACCT-01 and AUTH-03): `username` string R, `password` string R.
- **TokenResponse** (AUTH-01): `access_token`, `token_type`, `refresh_token`, `expires_in`, `scope`.
- **PrincipalResponse** (AUTH-02): the serialised `java.security.Principal` of the current authentication. The field asserted by the baseline test suite is `name`.

Normalisation, from `statistics-service/.../StatisticsServiceImpl.java` and
`.../ExchangeRatesServiceImpl.java`: amounts convert to base currency USD and divide by the period
base ratio at scale 4 with `RoundingMode.HALF_UP`. Ratios: YEAR 365.2425, QUARTER 91.3106,
MONTH 30.4368, DAY 1, HOUR 0.0416.

## 5. Status code semantics

| Condition | Status | Body |
| --- | --- | --- |
| handler returns a value | 200 | the JSON document named in section 2 |
| handler returns void | 200 | empty |
| `@Valid` body fails validation | 400 | Spring error document |
| bearer token absent on a protected route | 401 | OAuth error document |
| bearer token present, scope rule denies | 403 | OAuth error document |

Evidence: `AccountControllerTest` and `UserControllerTest` assert 400 on their three validation
paths and 200 everywhere else; `StatisticsControllerTest` and `RecipientControllerTest` assert 200.

- S-1 a successful write that returned an empty body continues to return an empty body.
- S-2 a validation failure continues to be 400 and never 422.

## 6. Collections, clients and jobs

Stored collections (4). Every database is `piggymetrics` on port 27017, user `user`, password from
`MONGODB_PASSWORD`.

| Collection | Mongo host | Owner | Key |
| --- | --- | --- | --- |
| users | auth-mongodb | auth-service | username |
| accounts | account-mongodb | account-service | name |
| datapoints | statistics-mongodb | statistics-service | DataPointId(account, date) |
| recipients | notification-mongodb | notification-service | accountName |

Outbound clients (4).

| Caller | Declaration | Target | Fallback |
| --- | --- | --- | --- |
| account-service | AuthServiceClient.createUser | POST /uaa/users, JSON UTF-8 | none |
| account-service | StatisticsServiceClient.updateStatistics | PUT /statistics/{accountName}, JSON UTF-8 | StatisticsServiceClientFallback |
| notification-service | AccountServiceClient.getAccount | GET /accounts/{accountName}, raw JSON string | none |
| statistics-service | ExchangeRatesClient.getRates | GET ${rates.url}/latest?base={base} | ExchangeRatesClientFallback |

`rates.url` is `https://api.exchangeratesapi.io` in `shared/statistics-service.yml`.

Scheduled jobs (2), declared on `notification-service/.../NotificationServiceImpl.java` with values
from `shared/notification-service.yml`. Mail transport is `smtp.gmail.com` port 465 over SSL.

| Job | Cron | Property | Subject | Attachment |
| --- | --- | --- | --- | --- |
| backup | 0 0 12 * * * | backup.cron | PiggyMetrics account backup | backup.json |
| remind | 0 0 0 * * * | remind.cron | PiggyMetrics reminder | none |

- D-1 collection names and document key fields survive the rework, so an existing volume keeps resolving.
- OC-1 the three internal calls target the same paths the public inventory declares, so an inventory change is also a client change.
- OC-2 the two declared fallbacks keep absorbing downstream failure rather than surfacing it.
- J-1 cron expressions, mail subjects and the backup attachment name survive unchanged.

## 7. What this contract does not decide

This file is technology neutral by construction. It names paths, methods, auth rules, bodies,
status codes, collections, clients and jobs, and names no framework as a requirement. Platform
technology selection for plan P2 is tracked as confirmation item CQ-08 and is out of scope here.
