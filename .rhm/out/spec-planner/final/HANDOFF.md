# HANDOFF — Walk 22 Oct fresh

Execute `final/PLAYBOOK.md` and `final/DAG.yaml` literally. Do not invent architecture, files, coordinates, or markup.

## Mandatory rule

If a STEP exact_snippet is present, write that final text. If a coordinate is imported by that text and missing from the tree, add the coordinate declared in the STEP POM snippet. Do not lower acceptance below `mvn test`.

## Pins reminder

PLAYBOOK pins use labeled `~~~lang` fences per `docs/PLAYBOOK_FORMAT.md`.

## Critical path

STEP-001 → module POM STEPs → STEP-011 Dockerfiles → STEP-012/013 Gateway → STEP-014..029 security/Jakarta/fallbacks → STEP-018 turbine deletion → STEP-030 `mvn test`.

## Consumer decisions

- monitoring: D-009 Actuator host, no Hystrix Dashboard
- turbine-stream-service: D-010 named deletion

## Gate

~~~bash
mvn test
~~~
