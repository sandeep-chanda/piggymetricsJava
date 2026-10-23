---
name: generate-plan
description: Spec→Playbook+iDAG. Stack-agnostic Replit-grade 10/10. HM Harness (Omnigent/Claude) — no Cursor Plan Mode.
---

# /generate-plan

## Mode (HM Harness)

Writes only under `workspace/<task-id>/`. Never modify TargetRepo.

Research / STEPs / iDAG / critic run as a normal Omnigent + Claude Code agent turn.
**Do not** use Cursor `SwitchMode`, Plan Mode, or `playbook-importer`.

`final/PLAYBOOK.md` is authored **directly** (same letter-to-letter depth as Cursor
`.plan.md` body + todos ≈ STEPs). Then emit the RHM wire JSON
(`type: spec_planner_package`) in the ASSIST reply.

## Inputs

1. Spec path  
2. TargetRepo path  
(+ optional related repos / connection info)

Bootstrap if needed:

```bash
python scripts/init-task.py --spec <SPEC> --target <TARGET> \
  --connection <connection.meta.json> \
  --related <local-sibling-path>   # repeatable
```

Connection meta must include provider + `sibling_repos` + auth refs (`docs/CONNECTION.md`, `templates/connection.meta.json`).

## Quality

Follow `docs/QUALITY_BAR.md`, `docs/EXECUTION_SPEC.md`, `docs/PROTOCOL.md`, `docs/TOOLS.md`, `docs/RESEARCH.md`, `docs/PLAYBOOK_FORMAT.md` (pin placeholders still apply; ignore SwitchMode steps).

- Any Spec / any repo / any stack — discover; never assume  
- Letter-to-letter STEPs + full-skeleton iDAG in YAML (**unchanged — do not remove**)  
- PLAYBOOK = same STEP depth in `final/PLAYBOOK.md` (todos ≈ STEPs; body still letter-to-letter)

## Phases

1. Spec Analyst  
2–6. Environment + Repo (+ Cross-repo if needed) + External  
7–8. Architect options + decisions  
9. Migration if triggered else N/A  
10. Planner — letter-to-letter STEPs (`docs/EXECUTION_SPEC.md`) — **same depth as before**  
11. DAG Builder — full skeleton nodes  
12. Critic + attack log (FAIL→Gap→rework; max 3)  
13. Handoff simulation  
14. Scorecard (all 10) + Quality Gate on YAML artifacts  
15. **PLAYBOOK (HM — write directly):**

    **Constructive only:** do not drop STEPs, pins, verify, or coverage.

    1. Write `workspace/<task-id>/final/PLAYBOOK.md` with:
       - **todos / STEPs** covering every residual STEP (STEP ids in content or body)
       - **body** with the prior depth (files, exact artifacts, verify, do-not)
       - **pins** per `docs/PLAYBOOK_FORMAT.md`: `exact_snippet` / `exact_markup` /
         `exact_companion` as `~~~<lang>` fences; `exact_commands` as `~~~bash`
    2. Write `final/HANDOFF.md`, `final/DAG.yaml` (or idag), `final/PACKAGE_MANIFEST.yaml`
    3. Run pack validators when scripts exist (`validate-playbook`, `check-handoff`)
    4. Never invent thin STEPs; never call Cursor Plan Mode tools

## Exit

**HM hard rule:** Do **not** emit `quality_gate: FAIL` / blocked stubs because SwitchMode
is missing. Write real `final/` files first; only then emit wire JSON with `PASS`.

**Branch hard rule:** Never pin `cursor/hmh-*` / `rhm/pack/*` as the Spec delivery branch
(those are this session's checkout). Pin Spec/`main`/implementer-from-main only.

Print APPROVED/blocked, workspace path, validators, TargetRepo untouched, confirm
**STEP coverage still present**, PLAYBOOK/HANDOFF pins match `docs/PLAYBOOK_FORMAT.md`,
then emit **one** ```json``` `spec_planner_package` for RHM (see thin work prompt).
