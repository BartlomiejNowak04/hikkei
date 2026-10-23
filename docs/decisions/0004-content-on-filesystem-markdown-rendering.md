# 0004. Revision contents and blobs on the filesystem, Markdown rendered by flexmark

Reconstructed during Yulius onboarding (2026-10-23) from the code and its history.

## Status

Accepted (since May-July 2025).

## Context

Revisions are whole documents and uploads are binary files; keeping them in table rows makes
the history tables heavy. Pages need Markdown with links between pages.

## Decision

- `FilesystemVersionProvider` (`FILE_DIRECTORY`) stores each revision's content and
  `FilesystemFileProvider` (`IMAGE_DIRECTORY`) stores blobs, each as a UUID-named file; the
  database keeps the identifier (`<uuid>$<mime>`). Paths are normalised and must stay inside
  the directory.
- `MarkdownPageRenderer` renders with flexmark-java with the wiki-link (`[[Page]]`) and
  strikethrough extensions; the API offers raw and rendered content and a render preview.

## Alternatives

- Contents in PostgreSQL (`TEXT`/`BYTEA`): one store to back up, but larger tables.
- Object storage (S3): unnecessary for a single self-hosted server.
- CommonMark-java or a frontend-side renderer: flexmark has the needed extensions.

## Consequences

- Backups must cover the database and both directories together.
- The backend needs read-write access to the directories; tests use `.testfs/` (ignored).
