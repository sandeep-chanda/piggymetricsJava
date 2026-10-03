# Author confirmation ledger

Plan P1 todo P1.T1 asks for author confirmation where design intent is absent. Each row is a
behaviour the baseline exhibits and does not explain. Each row carries a default that applies when
no reply arrives, so plan P2 is never blocked on silence.

State is `OPEN`, `CONFIRMED` (the default stands) or `AMENDED` (the author chose otherwise, and the
linked contract section was edited in the same commit). Check A-5 in `ACCEPTANCE-CHECKS.md` parses
this table.

| Id | Area | Question | Default when no reply arrives | State |
| --- | --- | --- | --- | --- |
| CQ-01 | authorisation | Is the hard-coded account literal `demo` in ACCT-02 and STAT-02 a product feature or a leftover demo affordance? | treat it as contractual and preserve it | OPEN |
| CQ-02 | routing | ACCT-01 is mapped on the trailing-slash path, so the gateway path is `/accounts/`. Does `/accounts` without the slash also have to answer 200? | only the trailing-slash form is contractual | OPEN |
| CQ-03 | payload | AUTH-02 serialises the raw framework principal. Is the whole document contractual, or only the `name` field the baseline test asserts? | only `name` is contractual | OPEN |
| CQ-04 | payload | The notification-service outbound client types the account response as a raw string. Is the passthrough shape contractual? | the passthrough shape is contractual | OPEN |
| CQ-05 | external | The exchange rates client targets `https://api.exchangeratesapi.io/latest`, which now requires an API key. Is the live dependency contractual, or is the declared fallback the contract? | the fallback is the contract; the live call is best effort | OPEN |
| CQ-06 | security | The authorization server stores tokens in memory and compares client secrets without hashing. Preserved, or corrected during the rework? | preserve in P2; raise correction as separate work | OPEN |
| CQ-07 | security | The auth-service user type returns a null authority collection. Is the empty-authority behaviour contractual? | contractual, because resource servers key off scope rather than authority | OPEN |
| CQ-08 | platform | The spec lists Eureka, Feign, Zuul, Spring Security OAuth, Hystrix, Spring Cloud Config and Turbine as keep directives and also as replacement directives. Which governs plan P2? | the keep directive governs, because the spec scope line forbids a rewrite | OPEN |
| CQ-09 | repositories | The spec names `sandeep-chanda/piggymetricsJava` while this run is bound to `sandeep-chanda/PiggyMetricsDotNet`. Which repository receives the P2 and P3 commits? | P1 lands here as documentation; P2 and P3 land in the Java repository | OPEN |
| CQ-10 | numerics | Statistics normalisation divides at scale 4 with HALF_UP against fixed period ratios. Are scale and ratios contractual to the digit? | contractual to the digit | OPEN |

## Where each item comes from

- CQ-01 `AccountController.getAccountByName` and `StatisticsController.getStatisticsByAccountName` both carry `@PreAuthorize` with a literal `demo` comparison, and account-service additionally permits `/demo` anonymously. The answer changes Tier C rows C-7 and C-8.
- CQ-02 the mapping is `@RequestMapping(path = "/", method = RequestMethod.POST)` under the `/accounts` context path. The answer changes whether a second route joins the inventory.
- CQ-03 `UserController.getUser` returns `java.security.Principal` directly, so the document is framework shaped and unversioned. Resource servers consume it through `security.oauth2.resource.user-info-uri`, which makes its shape load bearing for the whole platform.
- CQ-04 `AccountServiceClient.getAccount` declares a `String` return and the notification backup job attaches that string verbatim, so a typed rewrite changes the attachment bytes.
- CQ-05 `rates.url` is `https://api.exchangeratesapi.io` and the client declares `ExchangeRatesClientFallback`, so a baseline run today exercises the fallback rather than the live service.
- CQ-06 `OAuth2AuthorizationConfig` uses `InMemoryTokenStore` and `NoOpPasswordEncoder`. Both are observable: tokens do not survive a restart, and secrets are compared verbatim.
- CQ-07 `com.piggymetrics.auth.domain.User.getAuthorities` returns null, so a rewrite returning an empty list puts a different document on the wire for AUTH-02.
- CQ-08 the spec architectural requirements section lists each of the seven technologies twice while the scope section says a rewrite is out. Plan P1 does not need the answer because this contract names no framework; plan P2 cannot start without it.
- CQ-09 `.harnessmatters/context/binding.json` names the .NET repository and the spec scope section names the Java repository. P1 is documentation and lands safely here; P2 reworks Maven modules that exist only in the Java repository.
- CQ-10 `StatisticsServiceImpl` divides by `TimePeriod.getBaseRatio` at scale 4 with `RoundingMode.HALF_UP`, and `ExchangeRatesServiceImpl` converts the same way against base currency USD. Ratios are YEAR 365.2425, QUARTER 91.3106, MONTH 30.4368, DAY 1, HOUR 0.0416. A different scale produces different stored datapoints.

## How to answer

Edit the State cell to `CONFIRMED` when the default stands. Edit it to `AMENDED` and edit the
linked section of `API-CONTRACT.md` in the same commit when the author chooses otherwise. Check
A-5 reports the open set and the contract stays unapproved while any row reads `OPEN`.
