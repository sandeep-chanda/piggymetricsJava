# HM Harness — Spec-Planner runtime notes

This pack lives under `agent-packs/HM Harness/spec-planner/`.
Cursor’s twin is `agent-packs/Cursor Cloud Harness/spec-planner/`.

## Runtime

| Concern | Cursor Cloud | HM Harness (this pack) |
|---------|--------------|-------------------------|
| Coding agent | Cursor Cloud VM | Omnigent + Claude Code in AKS factory image |
| Skill roots | `.cursor/skills` | Seed mirrors → `.claude/skills` + `.agents/skills` |
| PLAYBOOK | SwitchMode → Plan → playbook-importer | Write `final/PLAYBOOK.md` directly |
| Wire exit | `type: spec_planner_package` | Same |
| SCM | Integrations clone | Factory `clone-workspace.sh` + Connections PAT |

## Hard rules

1. Never require Cursor Plan Mode / SwitchMode.
2. Never invent production code into the target tree.
3. Same quality bar as Cursor pack (`docs/QUALITY_BAR.md`).
4. Factory image must expose: git clone, LiteLLM→AFD, RHM MCP, Stream A SSE — same
   as local `Omnigent-Foundry-Endpoint` when those deps are available in-cluster.
