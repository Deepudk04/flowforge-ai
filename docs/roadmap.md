# Roadmap

The detailed scope, exit criteria, and sequencing live in the
[flagship implementation plan](flagship-implementation-plan.md). This page is the concise status view.

## Completed Foundation

- Spring Boot and FastAPI service boundary with deterministic local mode.
- Backend-to-AI HTTP integration with timeout, bounded retry, error mapping, and correlation IDs.
- PostgreSQL persistence for generated documents, workflow diagrams, and generation jobs.
- Typed generation-job states, enforced transitions, and database status constraints.
- Short persistence transactions around job lifecycle changes; external AI calls run outside transactions.
- Mock provider and runtime-selected memory/pgvector retriever factories.
- Maven Wrapper, Docker Compose, CI, synthetic samples, and architecture documentation.

## Current Milestone: Phase 1

Workspace and approved-source foundation:

- Add organizations and workspaces.
- Add text, Markdown, and bounded PDF source ingestion.
- Add source hashing, duplicate detection, approval, rejection, and local artifact storage.
- Scope all new records and queries by organization.

## Later Milestones

1. Real provider integration and validated structured output.
2. Runtime RAG with approved-source filtering, scores, and persisted provenance.
3. Asynchronous generation, retries, idempotency, cancellation, and stale-job recovery.
4. Immutable versions, human review, and audit events.
5. Frontend workflows plus Markdown, PDF, and DOCX exports.
6. Security hardening, reliability controls, and observability.
7. Quality gates, evaluation fixtures, contract tests, and end-to-end system proof.

## Public Version Guardrails

- Keep all examples and seed data synthetic.
- Never commit provider credentials or private documents.
- Do not claim production usage.
- Keep planned capabilities clearly separated from implemented behavior.
