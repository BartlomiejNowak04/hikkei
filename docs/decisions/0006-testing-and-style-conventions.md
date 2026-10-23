# 0006. Test-driven development, Google Java Style and Conventional Commits

Reconstructed during Yulius onboarding (2026-10-23) from the README "Contributing" section,
`build.gradle`, `.githooks/pre-commit` and the history.

## Status

Accepted (since April-July 2025).

## Context

A team of students working in parallel needed shared rules for tests, formatting and history.

## Decision

- Test-driven development: every non-trivial change comes with tests; no class is left untested.
- JUnit 5 with jqwik registered as a second engine (`*PropTest` for property tests), Mockito
  (`*MockTest`), Vert.x JUnit 5 and REST Assured for route tests.
- Integration tests (`*IntegrationTest`) read database settings from `.test-env` or the
  environment and are skipped when they are missing (`IntegrationTest.require`).
- Java is formatted with google-java-format through spotless; `./gradlew check` fails on
  unformatted code and `.githooks/pre-commit` runs it before each commit.
- Commits follow Conventional Commits and good commit-message practice.

## Alternatives

- Testcontainers inside the tests: would make integration tests self-contained but adds a
  Docker dependency to every test run; CI provides the database instead ([ADR 0007](0007-yulius-ci-and-ratcheted-gate.md)).
- Checkstyle with Google rules: reports instead of fixing; spotless can `spotlessApply`.

## Consequences

- A local run without `.test-env` skips the integration tests silently; the CI script fails
  when any test is skipped while its database is up.
- Run `./gradlew spotlessApply` before committing Java changes.
