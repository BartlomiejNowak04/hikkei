# Changes

Newest first. One short entry per delivered change: what changed and where to read more.

## 2026-10-23: Onboarding to Yulius

- Added `yulius.toml`, `AGENTS.md`, `CLAUDE.md`, `docs/product.md`, `docs/architecture.md`,
  ADRs 0001-0007 (reconstructed from the code and history), `docs/specs/` and this log.
- CI moved from GitHub Actions to Yulius CI: jobs `scripts`, `backend` (all 373 tests against a
  disposable PostgreSQL; previously 108 were skipped) and `frontend` (ESLint, knip, bundle,
  type check), all from `scripts/ci/`. `.github/workflows/gradle.yml` is removed.
- New dead-code checks: PMD unused-code rules and Gradle dependency analysis (backend), knip
  (frontend). Existing findings are baselined (AGENTS.md "Ratcheted baselines", ADR 0007).
- `.env.example` lists the backend's environment variables. CD: nothing deploys yet.
