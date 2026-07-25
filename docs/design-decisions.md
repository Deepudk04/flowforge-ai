# Design Decisions

## Why Spring Boot Backend

Spring Boot is used for the public API because it gives the project a mature enterprise-style foundation: request validation, structured controllers, OpenAPI support, security integration, persistence boundaries, and predictable test tooling.

## Why FastAPI AI Service

FastAPI keeps AI orchestration in a Python service where provider SDKs, prompt tooling, retrieval logic, and model-specific experiments are easier to evolve independently from the Java API layer.

## Why Separate Backend and AI Service

The split keeps API ownership and AI experimentation separate. The backend can remain stable for clients while the AI service changes provider integrations, retrieval strategies, prompt versions, or evaluation code.

The formal ownership decision and consequences are recorded in [ADR 001](adr/001-spring-fastapi-ownership.md).

## Why PostgreSQL and pgvector

PostgreSQL handles normal metadata such as generated documents, workflow diagrams, and generation job records. pgvector allows retrieval embeddings to live beside operational data without adding a separate vector database to the local architecture.

## Why Mermaid for Diagrams

Mermaid diagrams are text-first, diffable, easy to render in GitHub, and suitable for workflow documentation. They also avoid binary diagram assets in a public portfolio repository.

## Why Mock LLM Provider Exists

The mock provider makes tests, CI, and local demos deterministic. It also prevents accidental dependency on real API keys, model quotas, or external provider availability.

## Why Prompt Templates Are Versioned

Versioned prompt templates make generation behavior reviewable. A prompt change becomes a normal code review artifact instead of an invisible runtime change.

## Sync vs Async Generation Trade-Off

Synchronous generation is simpler for the public version and easy to test through a single HTTP request. Production systems should use async jobs for long-running document generation, retries, cancellation, rate limiting, and auditability.

## Intentionally Simplified Public Version

- Backend endpoints call the FastAPI service by default; deterministic local mode remains available for tests and offline demos.
- The mock provider is the only implemented LLM provider.
- Retrieval uses synthetic local examples by default, with a pgvector adapter available through runtime configuration.
- No real customer data, proprietary prompts, private documents, or production credentials are included.
