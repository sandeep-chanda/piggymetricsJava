# Spec Planner

**Research-First Software Implementation Planner** (Implementation Determinism Engine).

## What it does

```
INPUT  = Spec + TargetRepo + connection (provider, siblings, auth refs) + optional related local paths
PROCESS = research → evidence → decisions → (migration) → playbook + iDAG → critic → gates
OUTPUT = AgentOutcomePackage  (Coding Playbook + Implementation DAG)
```

Cross-repo = sibling repos under the same SCM project/org root (e.g. Azure DevOps project repo list). See `docs/CONNECTION.md`.

Produces an **agent outcome** so any coding agent can implement the spec in the target repo with near-deterministic results.

## Hard operating mode: workspace-only (HM Harness)

While this planner runs on **HM Harness (Omnigent / Claude Code)**:

- Research, reason, and write **planner artifacts** under `workspace/<task-id>/`.
- **Never write production code** into the **target repository**.
- **Write `final/PLAYBOOK.md` directly** — no Cursor SwitchMode / Plan Mode /
  playbook-importer (those exist only on the Cursor Cloud twin pack).
- Target repo is **read-only context** (inspect, search, cite evidence).
- **Tools:** Bash / Read / Edit / Write / Skill (`/generate-plan`). Catalog: `docs/TOOLS.md`.

Downstream coding agents consume the package later.

## Quick start

1. Open this repo in Cursor (optionally add the target repo as a second workspace folder, read-only intent).
2. Run `/generate-plan` with paths to the spec and target repo.
3. Or: `python scripts/init-task.py --spec path/to/spec.md --target path/to/repo`
4. Inspect `workspace/<task-id>/final/` when quality gate PASSes.

## Layout

| Path | Role |
|------|------|
| `docs/` | PROTOCOL, RESEARCH, HANDOFF, TOOLS |
| `schemas/` | JSON Schema for all artifacts |
| `templates/` | Empty starters |
| `scripts/` | init-task + validators (used by hooks) |
| `workspace/` | Per-task artifact trees |
| `examples/` | Synthetic target repo, sample spec, golden package |
| `.cursor/` | Rules, agents, skills, hooks, mcp.json |

## Non-goals

- Production codegen into the target repo
- Custom agent runtime
- Runtime/Airflow DAG codegen as the primary product
