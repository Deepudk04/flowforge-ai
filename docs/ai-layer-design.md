# AI Layer Design

The service keeps AI provider calls behind a small provider interface. Application code sends structured requests to an orchestrator, which renders a prompt, attaches retrieval context, and calls the configured provider.

## Components

- `app.models`: request and response contracts
- `app.prompts`: prompt registry and renderer
- `app.retrieval`: retrieval interfaces, in-memory retriever, and pgvector adapter
- `app.pipeline`: document generation flow
- `app.diagrams`: Mermaid workflow diagram generation

## Local Defaults

Local runs use the mock provider and in-memory retrieval. The retriever factory selects pgvector when `RETRIEVAL_BACKEND=pgvector` and `VECTOR_DB_URL` is configured.

## Prompt Orchestration

Prompt templates live under `app/prompts/templates` and are resolved through the prompt registry. The renderer receives structured variables instead of assembling ad hoc prompt strings at each call site.

## Provider Boundary

`AIProvider` defines the generation interface. `MockAIProvider` is the only implemented provider, and configuration validation rejects unsupported provider names. A provider factory supplies the instantiated provider to orchestration and readiness reporting.

## Retrieval Boundary

Retrieval logic is hidden behind retriever interfaces. A runtime factory selects the in-memory retriever for deterministic tests and local examples or `PgVectorRetriever` for the PostgreSQL/pgvector adapter path. Readiness reports the selected retriever instance.
