# AI Layer Design

The service keeps AI provider calls behind a small provider interface. Application code sends structured requests to an orchestrator, which renders a prompt, attaches retrieval context, and calls the configured provider.

## Components

- `app.models`: request and response contracts
- `app.prompts`: prompt registry and renderer
- `app.retrieval`: retrieval interfaces, in-memory retriever, and pgvector adapter
- `app.pipeline`: document generation flow
- `app.diagrams`: Mermaid workflow diagram generation

## Local Defaults

Local runs use the mock provider and in-memory retrieval. pgvector can be enabled later by setting `RETRIEVAL_BACKEND=pgvector` and `VECTOR_DB_URL`.
