# Compatibility baseline (spec "Fresh data", plan P1)

This directory is the compatibility baseline for the PiggyMetrics public surface. It is the
approval gate named by plan P1: an endpoint-level compatibility contract and acceptance checks.
It contains documentation only. It contains no production code and no build wiring.

## Files

| File | Role |
| --- | --- |
| `API-CONTRACT.md` | human readable contract: routing, 12 endpoints, auth model, payload schemas, status semantics, 4 collections, 4 outbound clients, 2 scheduled jobs |
| `baseline.json` | machine readable twin of the contract, consumed by the Tier A checks |
| `ACCEPTANCE-CHECKS.md` | Tier A self-consistency checks, Tier B baseline capture, Tier C endpoint matrix |
| `CONFIRMATIONS.md` | ten items where the baseline shows behaviour it does not explain, each carrying a default |

## Provenance

Every statement in this directory was read out of `sandeep-chanda/piggymetricsJava` at commit
`f760b95a538cfc199b1ce8d7893e55cd7a7c22ac`. The baseline there is nine Maven modules on
`spring-boot-starter-parent` 2.0.3.RELEASE targeting Java 1.8, carrying 61 test methods across
25 test classes.

Five modules carry request handling behaviour: gateway, auth-service, account-service,
statistics-service, notification-service. Four modules are platform: config, monitoring,
registry, turbine-stream-service. Only the five behaviour modules appear in the contract.

## How to use it

Run the Tier A checks now:

~~~bash
python3 -c "import json;json.load(open('docs/compatibility/baseline.json'));print('parse ok')"
~~~

Then work through `ACCEPTANCE-CHECKS.md` Tier A in order. Tier B and Tier C need a composed
PiggyMetrics deployment and belong to plans P2 and P3.

## Relationship to the later plans

Plan P2 reworks the Maven modules governed by `pom.xml` while preserving the contract recorded
here. Plan P3 exercises `docker-compose.dev.yml` and `docker-compose.yml` and runs the checks
configured in `.travis.yml`. Both consume this directory as their definition of "unchanged
public API". Neither is in scope for plan P1.

Plan P2 is blocked on confirmation item CQ-08, which resolves the platform technology direction,
and on CQ-09, which resolves which repository receives those commits.

## Changing this contract

A change to the public surface is a change to `API-CONTRACT.md`, to `baseline.json` and to the
affected rows of `ACCEPTANCE-CHECKS.md`, in one commit. A contract edit that leaves the machine
twin behind fails check A-4.
