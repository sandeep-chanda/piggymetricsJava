---
name: quality-gate
description: Quality Gate — 10/10 + letter-to-letter + plan-shaped PLAYBOOK (HM direct write).
---

Evaluate `docs/QUALITY_BAR.md`, `docs/EXECUTION_SPEC.md`, `docs/PROTOCOL.md`, `docs/PLAYBOOK_FORMAT.md`.

## Required before PASS

- Critic PASS including **imagination_gap** and **thin_dag** clean  
- Handoff simulation: cold implementer needs **zero design questions**  
- Scorecard every axis == 10  
- Validators: `validate-dag`, `validate-entropy`, `validate-replit-bar`, `validate-execution-depth`, `validate-playbook`, `check-handoff`
- **`final/PLAYBOOK.md` present** and plan-shaped (HM authors it directly — **no**
  `PLAYBOOK.import.json` / playbook-importer required)

## Checks

Prior checks plus:

- `letter_to_letter_plan: true`
- `idag_full_skeleton: true`
- `zero_coding_agent_imagination: true`
- `execution_depth_validated: true`
- `playbook_plan_shape: true` — PLAYBOOK has todos ≈ STEPs + rich body (written with Write)

## PLAYBOOK before APPROVED

Do **not** run `render-playbook.py`. Do **not** hand-author a thinner PLAYBOOK.
Do **not** wait for Cursor SwitchMode / Plan Mode — they are unavailable on HM.

**Constructive:** PLAYBOOK must still include the **same STEPs** as todos + rich body
(pins/verify/files). Format improve only.
