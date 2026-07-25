# ADR 001: Spring Boot and FastAPI Ownership

- Status: Accepted
- Date: 2026-07-25

## Context

FlowForge needs stable public APIs and persistence while allowing prompt, retrieval, and model integrations to evolve independently. Mixing these responsibilities in one service would couple API lifecycle changes to AI experimentation.

## Decision

Spring Boot owns public HTTP contracts, validation, authorization boundaries, job lifecycle orchestration, operational metadata, generated-resource persistence, and API documentation.

FastAPI owns prompt rendering, provider selection, retrieval execution, AI-specific validation, model usage traces, and deterministic mock generation.

Spring calls FastAPI through `AiServiceClient`. The HTTP adapter is the normal runtime path, while a deterministic local adapter supports isolated tests and offline demos. Spring does not keep a database transaction open during an AI call.

## Consequences

- Public API and persistence contracts can remain stable while AI implementation details change.
- Cross-service contracts require explicit contract tests and versioning.
- Correlation IDs and error mapping must cross the boundary.
- A two-service deployment has more operational overhead than a monolith.
- Future queue workers remain backend-owned and invoke the AI boundary rather than moving job state into FastAPI.
