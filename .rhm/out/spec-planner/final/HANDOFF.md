# Coding Agent Handoff

> Execute the implementation playbook and iDAG **literally**.
> Do **not** reinterpret architecture.
> Do **not** invent files, APIs, or commands.
> If anything is unspecified, **stop** — that is a planner defect.

## Plan Mode note

This package was produced in **Plan Mode** (HM harness: SwitchMode skipped; artifacts under `.rhm/out/spec-planner/`). The planner did **not** modify production code in the target repository.

## Package

- PLAYBOOK.md — Cursor-shaped plan with STEP depth
- DAG.yaml — iDAG full skeleton
- Linked research/decisions under `../`

## Spec

walk-e2e-20261024-scenario · Plan key **P1** · Target **piggymetricsJava** account-service

## Concrete change

Add settlement observability to account-service:

1. p95 latency Timer `account.settlement.latency`
2. failed-batch Counter with retry via `account.settlement.retry.*`
3. `GET /observability/dashboard` reading both
4. Extend tests P1.T7–T11

## Commit messages

Name todo ids completed, e.g. `feat(account): P1.T1 P1.T2 dashboard and Account fields`.

## Verify

exact_commands:

~~~bash
mvn test
~~~

## Out of scope

auth-service, gateway, monitoring, notification-service, registry, turbine-stream-service production edits (config shared yml for account-service keys only). Do not raise JVM major; keep `FROM java:8-jre`.
