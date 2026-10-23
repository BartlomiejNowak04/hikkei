# 0001. Java 21 + Vert.x backend, Next.js frontend in one repository

Reconstructed during Yulius onboarding (2026-10-23) from the code and its history.

## Status

Accepted (in effect since April-July 2025).

## Context

hikkei is a team-built wiki. The repository started from a Java/Gradle classroom template
(April 2025) and gained an HTTP API in July 2025 and a separate web UI in `frontend/` later.

## Decision

- The backend is Java 21, built with Gradle (wrapper 8.5), with Vert.x 5 (`vertx-web`) for an
  asynchronous HTTP API under `/api/*`; blocking JDBC work runs in `blockingHandler`s.
- The frontend is a Next.js 15 (App Router, React 19, TypeScript, Tailwind/PostCSS, CSS
  modules) application in `frontend/`, talking to the backend only through that API with a
  cookie session and CORS (`FRONTEND_URI`).
- Both live in one repository; the root Gradle project is the backend.

## Alternatives

- Spring Boot: more conventions and startup weight; the team chose the lighter Vert.x router.
- Server-rendered pages from the backend: rejected in favour of a separate React UI.
- Two repositories: more overhead for a small team; the API contract is the README section.

## Consequences

- Two toolchains (JDK 21 and Node/npm) and two CI jobs.
- The API contract lives in README "Backend API" and must be updated with every endpoint change.
- CORS and cookie settings couple the two deployments (`FRONTEND_URI`, `NEXT_PUBLIC_API_BASE`).
