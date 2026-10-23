# Product: hikkei

*A minimalistic yet feature-full wiki software*, built by team-06 (a university team
project started from a GitHub Classroom template in April 2025). The README's feature list is
the product backlog; this page summarises what exists in the code today.

## Purpose

A self-hosted wiki: people read and write articles ("pages") grouped in categories, every
edit is a revision that a moderator approves or rejects, and readers discuss pages in
comments. Content is written in Markdown with `[[wiki links]]` between pages.

## Users

- **Readers** browse categories, search pages by title, open a random article and read the
  current approved revision.
- **Editors** sign in (GitHub or Google OAuth2), write revisions, keep drafts, preview the
  rendered Markdown and comment.
- **Moderators and admins** approve or reject revisions, archive pages, manage categories and
  grant users per-category permissions.
- **The site operator** runs the backend, PostgreSQL and the frontend, and signs in as the
  built-in `admin` super user with the `ADMIN_PASS` token.

Permissions are a ladder `NO_ACCESS < UNTRUSTED < TRUSTED < MODERATOR < ADMIN < SUPER_ADMIN`;
a user's effective level in a category is the higher of their global level and the category's
level (the category default, or the level granted to that user in it).

## Scope

In scope (backend API in the README, "Backend API"; screens under `frontend/src/app`):

| Area | What it does |
| --- | --- |
| Users | sign up and sign in with OAuth2 (GitHub, Google) or the admin token, rename, profile picture, delete account, list a user's pages, revisions, comments and category permissions |
| Pages | create in a category, rename, move to another category, archive and unarchive, delete, search by title, random page |
| Revisions | create, save and delete drafts, preview (render Markdown), list per page or user, approve or reject, view raw or rendered |
| Comments | add, delete, list per page or user |
| Categories | create, rename, delete, default permission, per-user permissions |
| Blobs | upload and serve binary files (images) with their MIME type |

Out of scope today: the Wiki Game from the README backlog, full-text search, deployment
tooling (there is no production deployment yet, see [ADR 0007](decisions/0007-yulius-ci-and-ratcheted-gate.md)).

## Current state (2026-10)

- Backend (Java 21, Vert.x 5) implements every endpoint in the README with 373 unit and
  integration tests; the integration tests run against a real PostgreSQL.
- Frontend (Next.js 15, React 19) covers the landing page, articles, revisions and editor,
  comments, categories, account settings and the sign-in flow. It has no tests yet, and its
  type check and ESLint have known findings that are baselined, not fixed (see AGENTS.md).
- Several fix branches exist on GitHub that were never merged (`fix/auth-failure-handler`,
  `fix/permission-levels`, `fix/wikilink-slug`, ...); `main` is the source of truth.
- The README feature checklist is unticked although most features exist; it is the original
  plan, not a status report.
