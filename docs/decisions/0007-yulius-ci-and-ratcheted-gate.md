# 0007. Yulius CI jobs with a disposable PostgreSQL and ratcheted lint baselines

## Status

Accepted (2026-10-23, Yulius onboarding).

## Context

The only CI was a GitHub Actions workflow (`.github/workflows/gradle.yml`) running
`./gradlew build` on push without a database, so 108 of 373 tests were skipped, and nothing
checked the frontend. Yulius runs CI on our own machines and forbids GitHub Actions. On the
current code `npm run lint` fails (27 ESLint errors), `next build` fails on two type errors,
and the dead-code tools report findings; onboarding may not change application code.

## Decision

- `yulius.toml` `[[ci.jobs]]` runs three jobs, each a committed script that installs what it
  needs into the job's own `$HOME` (Gradle distribution and dependencies, npm cache):
  - `scripts` - `scripts/ci/scripts.sh`: shellcheck of the repository's shell scripts;
  - `backend` - `scripts/ci/backend.sh`: starts a disposable `postgres:16.10-alpine` container
    (pinned by digest) on a random port, exports the test settings, runs
    `./gradlew cleanTest build buildHealth` and fails if any test was skipped;
  - `frontend` - `scripts/ci/frontend.sh`: `npm ci`, ESLint, `next build` in compile mode,
    `tsc --noEmit` against a baseline, and knip.
  `scripts/ci/gate.sh` runs all three in order as `[commands] gate`.
- Existing findings are ratcheted, not fixed: ESLint errors in
  `frontend/eslint-suppressions.json` (ESLint bulk suppressions; unused suppressions fail) and a
  warning cap, TypeScript errors per file and code in `frontend/tsc-baseline.json`, knip
  findings in `frontend/knip.jsonc`, PMD unused-code findings per rule and file in
  `config/pmd/unused-code.xml`, unused Gradle dependencies in the `dependencyAnalysis` block of
  `build.gradle`. New findings fail; fixing one requires shrinking the baseline.
- Dead-code tools: PMD 7 (`UnusedPrivateField`, `UnusedPrivateMethod`, `UnusedLocalVariable`,
  `UnusedFormalParameter`, `UnusedAssignment`, `UnnecessaryImport`) as part of `check`, the
  Gradle dependency-analysis plugin (unused dependencies only fail), and knip 6.42.1 run
  through `npx` (pinned) so that the frontend lockfile is not regenerated.
- `.github/workflows/gradle.yml` is deleted; its build-artifact upload is not replaced
  (nothing consumed it).
- CD: nothing is deployed today (no deploy scripts, containers or platform files), so
  `[cd.dev]` and `[cd.prod]` are empty tables with an `about` line.

## Alternatives

- One `gate` command only: simpler, but the backend and frontend would not run in parallel.
- Fixing the lint and type errors now: changes application code, out of scope for onboarding.
- Disabling type checking in `next.config.ts` (`ignoreBuildErrors`): would hide new errors
  too; compile mode plus the baseline keeps them visible.
- PostgreSQL from apt inside the job: needs root-owned system state and `initdb` as non-root;
  a container is disposable and isolated between concurrent runs.
- Adding knip to `devDependencies`: npm 11 rewrote unrelated lockfile entries (dropped other
  platforms' optional packages), so it runs pinned through `npx` instead.

## Consequences

- CI needs Docker, JDK 21, Node 24, Python 3 and shellcheck on the sandbox (installed during
  onboarding); the jobs install everything else themselves.
- `npm run lint` (`next lint`) still fails because it ignores the suppressions file; the gate
  uses `npx eslint src` instead. The production `next build` (with type checking) fails until
  the baselined type errors are fixed.
- The "Ratcheted baselines" table in AGENTS.md tracks removing every entry (GitHub issues are
  disabled on this repository).
