# 0003. PostgreSQL through plain JDBC with an append-only, history-keeping schema

Reconstructed during Yulius onboarding (2026-10-23) from the code and its history.

## Status

Accepted (since July 2025).

## Context

A wiki needs the history of names, titles, categories and approvals, and soft deletion so
links and revisions keep their authors.

## Decision

- PostgreSQL via the JDBC driver and hand-written SQL in `provider/JDBC/*`; no ORM.
- The schema (`src/main/resources/dev/tfkls/hikkei/database`) keeps each changing attribute in
  a dated history table (`user_name`, `page_title`, `page_category`, `revision_approved`, ...).
- `rules.sql` makes `UPDATE` and `DELETE` no-ops on the history tables; entities are deleted
  by setting `deleted` and `date_deleted`.
- `views.sql` exposes the latest state (`*_with_data`); `triggers.sql` makes inserts through
  the views write the history rows and enforces unique current usernames and page titles.
- The schema is applied by hand with `drop.sql` then `create.sql` (no migration tool).

## Alternatives

- An ORM (Hibernate/JPA): poor fit for the view- and trigger-based temporal model.
- Mutable rows with an audit log: simpler queries, but history becomes a second source.
- A migration tool (Flyway, Liquibase): not adopted yet; schema changes recreate the database.

## Consequences

- Reads are simple selects on views; history is free.
- Changing the schema means editing the SQL files and recreating the database: there is no
  upgrade path for existing data yet (add a migration tool before the first real deployment).
- Integration tests need a real PostgreSQL ([ADR 0006](0006-testing-and-style-conventions.md)).
