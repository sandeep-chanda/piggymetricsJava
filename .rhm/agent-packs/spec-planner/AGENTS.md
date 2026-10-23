# Agent instructions — Spec Planner (HM Harness)

Spec → Playbook + iDAG for **any Spec + any target repo + any stack**.

- **Never** write production code into the target repository.  
- Writes only under `workspace/<task-id>/`.  
- **Discover** technology from the target tree — never assume a stack.  
- Entry: `/generate-plan` — `.cursor/skills/generate-plan/SKILL.md` (also seeded as
  `.claude/skills/generate-plan/` for Omnigent/Claude discovery).  
- **No Cursor SwitchMode / Plan Mode** on this harness — write `final/` directly, then
  emit RHM wire JSON (`type: spec_planner_package`) in the ASSIST reply.  
- Outcome markdown (PLAYBOOK/HANDOFF): pin placeholder in `docs/PLAYBOOK_FORMAT.md` —
  labeled `~~~<lang>` fences; never a loose dump. **Write `final/PLAYBOOK.md` yourself** —
  ignore any leftover SwitchMode / playbook-importer language elsewhere in the pack.  
- **Never** emit `quality_gate: FAIL` stubs because SwitchMode is unavailable — keep writing
  until PASS artifacts exist (or a real Spec-level block after research).  
- **Never** pin HM agent ephemeral branches (`cursor/hmh-*`, `rhm/pack/*`) as the Spec
  delivery / push branch. Those names are planner-session checkouts only. Delivery branch =
  Spec-named feature branch, or `main` / repo default, or “implementer work branch from main”.
- Quality: `docs/QUALITY_BAR.md` + `docs/EXECUTION_SPEC.md`  
- Runtime notes: `HARNESS.md`  
- Docs: `docs/PROTOCOL.md`, `docs/RESEARCH.md`, `docs/HANDOFF.md`, `docs/TOOLS.md`, `docs/PLAYBOOK_FORMAT.md`  
