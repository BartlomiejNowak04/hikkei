# 0002. Domain as provider interfaces with swappable implementations

Reconstructed during Yulius onboarding (2026-10-23) from the code and its history.

## Status

Accepted (since May-July 2025).

## Context

Routes need users, pages, revisions, comments, categories, blobs, content storage, rendering
and authentication, and the team wanted to unit-test them without a database or OAuth server.

## Decision

`model/*` declares one interface per capability (`UserDataProvider`, `PageDataProvider`,
`VersionProvider`, `FileProvider`, `PageRenderer`, `AuthenticationProvider`,
`SessionProvider`, `PermissionVerifier`, ...) and records for the data. `provider/*` holds the
implementations (`JDBC*`, `Filesystem*`, `Markdown*`, `Standard*`, `Dummy*`). `Main` wires
them by hand (constructor injection, no DI framework) and passes them to `MainVerticle`.
Errors cross the boundary as `BadDataException` (client error) and `InternalErrorException`
(server error).

## Alternatives

- A DI framework (Guice, Dagger, Spring): unnecessary for one composition root.
- Calling JDBC from the route handlers: untestable without a database.

## Consequences

- Controllers are tested with Mockito mocks of the interfaces; providers get their own
  integration tests.
- A new storage or auth method is a new implementation plus one line in `Main`.
- `Main` grows with every provider; keep wiring there, not in the handlers.
