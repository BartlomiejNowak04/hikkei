# 0005. OAuth2 sign-in, server-side sessions and a permission ladder

Reconstructed during Yulius onboarding (2026-10-23) from the code and its history.

## Status

Accepted (since July 2025).

## Context

The wiki must not store passwords, needs moderators per category, and an operator account
from the first start.

## Decision

- Sign-in through OAuth2 providers (GitHub, Google via Vert.x OAuth2) enabled by their
  `AUTH_*` variables, plus a token method `admin` (`ADMIN_PASS`) for the built-in `admin`
  super user that `Main` creates or promotes at start-up. Users can attach several methods.
- Sessions are server-side and in memory (`StandardSessionProvider`, 7-day expiry) behind the
  `HikkeiSession` cookie. Without any auth method configured, dummy providers disable auth.
- Permissions are the ordered levels `NO_ACCESS < UNTRUSTED < TRUSTED < MODERATOR < ADMIN <
  SUPER_ADMIN`. The effective level is the higher of the user's global level and the category
  level (the user's grant in that category, else the category default); each endpoint asks
  the `AccessManager` for the level it needs.

## Alternatives

- Passwords: storage, reset and hashing burden.
- JWTs: stateless, but no global logout; `logout/global` needs server state.
- Role sets instead of a ladder: more flexible, more complex for a small wiki.

## Consequences

- Restarting the backend signs everybody out; running more than one backend instance needs
  a shared session store.
- Every new endpoint must state its required level; permission bugs are high-risk changes.
