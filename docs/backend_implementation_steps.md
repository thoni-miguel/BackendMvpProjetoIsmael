# Backend Implementation Steps (SQLite MVP)

## 1. Project Bootstrap
- Scaffold Ktor server module with Gradle, enable application + serialization plugins, and set up configs per environment (local/staging).
- Commit initial folder structure, logging config, and Dockerfile/docker-compose using API + SQLite volume.

## 2. Configuration + Dependency Wiring
- Centralize config (Hocon/Typesafe) for API keys, ports, rate limits, storage paths, and SQLite DSN.
- Add ktor-server, kotlinx.serialization, coroutine logging, and database libraries (e.g., Exposed or Ktorm) plus migration tooling (Flyway/LIquibase) that supports SQLite.

## 3. Database Schema & Migrations
- Translate documented entities (`employees`, `epi_types`, `deliveries`, `audit_log`, `media_files`) into SQL migrations with audit fields, foreign keys, and indexes on `updated_at` and `synced_at`.
- Include bootstrap seed script for fake data and ensure migrations run on startup and via `make migrate`/Gradle task.

## 4. Repository & Domain Models
- Create Kotlin data classes mirroring DB schema plus serialization annotations.
- Implement repository layer for CRUD/upsert logic, batching, and conflict detection using `updated_at` semantics, keeping SQLite-specific pragmas (foreign keys ON, WAL mode) configured.

## 5. HTTP Layer & Routes
- Define request/response DTOs with validation and map them to repositories.
- Implement endpoints per spec: employees, epi types, deliveries, audit log, media upload/download, including pagination (`updated_after`) and conflict responses (409 with current state payload).

## 6. Media Handling
- Set up storage directory (e.g., `/var/media` in container, configurable on host) and ensure secure file naming and checksum calculation upon upload.
- Process multipart uploads, persist metadata in `media_files`, and wire download route with authentication + content-type negotiation.

## 7. Sync & Conflict Workflow
- Implement batch sync handlers that check `updated_at`, set `synced_at`, and return confirmation payloads per entity.
- Document error contracts (409 conflicts, validation errors) and ensure SQLite transactions wrap batch operations for consistency.

## 8. Security, Observability & Testing
- Enforce API key middleware, HTTPS (behind reverse proxy/ngrok), and simple rate limiting (per device key) using Ktor features.
- Emit audit log entries on every mutation, including actor info and media events.
- Add unit/integration tests covering conflict cases and upload round-trips, plus OpenAPI generation for documentation.

## 9. Release & Ops
- Provide Makefile/Gradle tasks for running server, tests, migrations, and seeding locally.
- Document deployment steps for local Docker, staging VM, and guidance for future PostgreSQL migration once scaling demands change.
