# RAG Pipeline

The AI service contains retrieval-oriented building blocks without committing private documents or requiring a production vector database.

## Pipeline

```mermaid
flowchart TD
    Input[Generation Request] --> Query[Create Retrieval Query]
    Query --> Retriever{Retriever}
    Retriever --> Memory[In-Memory Demo Chunks]
    Retriever --> PgVector[pgvector Adapter]
    Memory --> Context[Ranked Context]
    PgVector --> Context
    Context --> Prompt[Prompt Renderer]
    Prompt --> Provider[Mock or Configured Provider]
    Provider --> Result[Generation Result]
```

## Current Implementation

- `InMemoryRetriever` supports deterministic local tests and demos.
- `PgVectorRetriever` defines the production-oriented adapter boundary for PostgreSQL with pgvector.
- `HashEmbeddingProvider` creates deterministic local embeddings for adapter-level tests and public examples.
- `DocumentGenerationPipeline` retrieves context, enriches the generation request, and delegates prompt rendering/provider execution to the orchestrator.

## Retrieval Contract

Retrieval requests include a query and limit. Retrieval responses include:

- source chunk ID
- title
- content
- metadata
- normalized score

## Public Version Constraints

The repository does not include real document corpora, embedding indexes, private files, or production vector database credentials. Synthetic chunks are used to demonstrate the contract safely.
