# Handoff Contract

## What the coding agent receives

Under `workspace/<task-id>/final/`:

- `PLAYBOOK.md` — **plan-shaped** playbook **written directly** on HM Harness (same
  letter-to-letter depth as Cursor Plan Mode). See `docs/PLAYBOOK_FORMAT.md`.  
- Optional `PLAYBOOK.hm.json` — authorship receipt (not required)  
- `DAG.yaml` — iDAG with **full skeleton** per node  
- `HANDOFF.md`  
- `PACKAGE_MANIFEST.yaml`

See `docs/EXECUTION_SPEC.md`. Cursor Cloud’s twin may also ship `PLAYBOOK.import.json`
from playbook-importer — that path is **not** used on HM.

## Mandatory instruction to coding agents

> Execute the implementation playbook and iDAG **literally**.  
> Do **not** reinterpret architecture.  
> Do **not** invent files, markup, copy, styles, APIs, or commands.  
> If anything is unspecified, **stop** — that is a planner defect, not a license to invent.

## Must already be captured (in-depth) — unchanged requirement

These must still be present in STEPs YAML **and** reflected in PLAYBOOK todos/body:

- Final artifact text for every product change (`exact_snippet` / companion / commands), written in PLAYBOOK/HANDOFF with the placeholder in `docs/PLAYBOOK_FORMAT.md` (labeled `~~~<lang>` fence; never a loose dump)  
- Before/after or line anchors  
- Invariants + edge cases  
- Machine + human verification  
- Spec clauses mapped to STEPs  
- Sibling/connection context when provided  
- Decisions and rejected alternatives  

## Handoff test

> Cold human **or** coding agent **or** traditional implementer, given only Spec + `final/` + TargetRepo:  
> Can they implement **to the letter** with **zero design questions** and **zero invented content**?

- Any design question → planner FAIL  
- Any invented content → planner FAIL  
