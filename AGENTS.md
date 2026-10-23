# AGENTS.md

hikkei is a minimalistic wiki: a Java 21 / Vert.x 5 backend (root Gradle project) with a
PostgreSQL database, and a Next.js 15 frontend in `frontend/`. The README is the original
project documentation (features, API, setup) and stays authoritative for the API and setup;
this file adds the rules, commands and layout agents need.

## Rules (from README "Contributing")

- **Test-driven development.** Every non-trivial change comes with tests; no class is left
  untested unless for very good reasons. Write the failing test first.
- **Google Java Style**, enforced by spotless (google-java-format 1.26.0): run
  `./gradlew spotlessApply` before committing Java or Gradle changes.
- **Conventional Commits** (`feat(scope): ...`, `fix(...)`, `test`, `docs`, `build`,
  `refactor`, `style`, `chore`), following good commit-message practice. Scopes used so far:
  `backend`, `frontend`, `database`, `auth`, `api`, `model`, `provider`, `deps`, `test`, `fmt`.
- **Git hook.** `.githooks/pre-commit` runs `./gradlew check`; enable it once per clone or
  worktree with `git config core.hooksPath .githooks`. Never bypass it with `--no-verify`.
- **README is the API contract.** Update README "Backend API" with every endpoint change and
  README "Setup" with every new environment variable (and `.env.example`).
- **Database schema** lives in `src/main/resources/dev/tfkls/hikkei/database`; it is
  append-only (no `UPDATE`/`DELETE` on history tables, soft delete), see ADR 0003. There is no
  migration tool yet: schema changes mean `drop.sql` + `create.sql`.
- **Permissions** are high-risk: every endpoint states the `PermissionLevel` it needs (ADR 0005).
- **No secrets in the repository.** Configuration goes in the ignored `.env` (names in
  `.env.example`) and `.test-env` for integration tests; the frontend uses `frontend/.env.local`.

## Commands

| What | Command |
| --- | --- |
| Whole gate (what CI runs) | `scripts/ci/gate.sh` |
| Backend: format, compile, all tests against a disposable PostgreSQL, PMD, unused deps | `scripts/ci/backend.sh` (needs Docker) |
| Backend tests only | `scripts/ci/backend.sh cleanTest test` |
| Backend without Docker (integration tests skip) | `./gradlew check` |
| Format Java | `./gradlew spotlessApply` |
| Frontend: install, ESLint, knip, bundle, type check | `scripts/ci/frontend.sh` (`lint` or `build` for a part) |
| Shell scripts | `scripts/ci/scripts.sh` |
| Run backend / frontend locally | `./gradlew run` / `cd frontend && npm run dev -- -p 20000` (see README "Start-up") |

Toolchains: JDK 21 (`openjdk-21-jdk-headless`), Node 24 / npm 11, Python 3, Docker,
shellcheck; Gradle 8.5 comes from the wrapper. Integration tests read `POSTGRES_HOST`,
`POSTGRES_NAME`, `POSTGRES_USER`, `POSTGRES_PASS` and `PORT` from the environment or
`.test-env`; `scripts/ci/backend.sh` provides them and fails if any test is skipped. Use ports
20000-20019 for development servers in Yulius sandboxes.

## Ratcheted baselines

The gate fails on new lint, type and dead-code findings; findings that predate it are
recorded (ADR 0007). When you fix one, shrink its baseline in the same change; never add
entries to make a change pass.

| Check | Baseline | Burn-down |
| --- | --- | --- |
| ESLint errors | `frontend/eslint-suppressions.json` (27); warnings capped at 11 in `scripts/ci/frontend.sh` | fix, then `npx eslint src --prune-suppressions` |
| TypeScript | `frontend/tsc-baseline.json` (2 errors; they make a plain `next build` fail) | fix, then lower the count |
| knip | `frontend/knip.jsonc` (`Forbidden.tsx`, `lucide-react`, 2 exports in `ApiCaller.ts`) | delete or use, then drop the entry |
| PMD unused code | `config/pmd/unused-code.xml` `violationSuppressXPath` per rule and file | fix, then drop the file name |
| Unused Gradle deps | `build.gradle` `dependencyAnalysis` excludes (`flexmark-all`, `vertx-web-client`, `jqwik`) | remove or replace, then drop the exclude |

`npm run lint` (`next lint`) ignores the ESLint suppressions and fails until the baseline is
gone; use `scripts/ci/frontend.sh lint`. GitHub issues are disabled on this repository, so
this table is the burn-down tracker until they are enabled.

## Layout

```
src/main/java/dev/tfkls/hikkei/
  Main.java              composition root: .env, providers, auth methods, MainVerticle
  model/                 domain records and provider interfaces (ADR 0002)
  provider/              implementations: JDBC/, filesystem/, auth/, page/, permission/
  server/                MainVerticle, routes/ (endpoints), controllers/, handlers/, util/
  util/                  small helpers
src/main/resources/dev/tfkls/hikkei/database/   create.sql, drop.sql, schema/views/rules/triggers
src/test/java/dev/tfkls/hikkei/                  unit (*Test, *MockTest) and *IntegrationTest
frontend/src/app/        Next.js routes;  frontend/src/components/ UI;  frontend/src/lib/ API client
scripts/ci/              gate scripts run by Yulius CI
config/pmd/              PMD unused-code ruleset
docs/                    product, architecture, decisions, specs, changes
```

## Documentation (Yulius)

Read in this order before building: `docs/product.md` (what and for whom),
`docs/architecture.md` (components, data flow, Mermaid diagram), `docs/decisions/` (ADRs
`NNNN-<slug>.md`: Status, Context, Decision, Alternatives, Consequences; numbers from
`record_number`), `docs/specs/` (task specs), `docs/changes.md` (newest first). `yulius.toml`
maps these documents and holds the commands, CI jobs and CD environments. Keep every document
true in the same change that alters what it describes.

## Delivery

Branches `feat/<n>-<slug>` (or `fix/...`), pull requests squash into `dev`; only a person
promotes `dev` to `main`. CI runs on our own machines through Yulius CI (`yulius.toml`
`[[ci.jobs]]`); there are no GitHub Actions. Nothing deploys yet: `[cd.dev]` and `[cd.prod]`
are empty. No Vidoc security scan runs on this instance (no key configured).

## Related repositories

- `Surowizator/hikkei`: the upstream this repository was forked from (team-06's original).
  Fetch it with `reference_repo` only when work needs its history; this fork is the source.
