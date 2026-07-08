# Design Decisions

## Why Spring Boot Backend

Spring Boot is used for the public API because it gives the project a mature enterprise-style foundation: request validation, structured controllers, OpenAPI support, security integration, persistence boundaries, and predictable test tooling.

## Why FastAPI AI Service

FastAPI keeps AI orchestration in a Python service where provider SDKs, prompt tooling, retrieval logic, and model-specific experiments are easier to evolve independently from the Java API layer.

## Why Separate Backend and AI Service

The split keeps API ownership and AI experimentation separate. The backend can remain stable for clients while the AI service changes provider integrations, retrieval strategies, prompt versions, or evaluation code.

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

- Backend endpoints currently use deterministic local generation behavior.
- The backend-to-AI-service HTTP path is represented as a boundary and roadmap item rather than a fully wired production integration.
- Retrieval uses synthetic local examples by default.
- No real customer data, proprietary prompts, private documents, or production credentials are included.
