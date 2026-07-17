# System Architecture

FlowForge AI is split into a Spring Boot backend and a Python FastAPI AI service. The backend owns public API contracts, validation, persistence, OpenAPI metadata, job lifecycle tracking, and optional local deterministic generation paths. The AI service owns prompt rendering, retrieval abstractions, provider selection, and AI pipeline code.

## System View

```mermaid
flowchart LR
    Client[API Consumer] --> Backend[Spring Boot Backend]
    Backend --> MetadataDB[(PostgreSQL Metadata DB)]
    Backend --> AI[FastAPI AI Service]
    AI --> PromptRegistry[Prompt Registry]
    AI --> Retriever[RAG Retriever]
    Retriever --> VectorDB[(PostgreSQL + pgvector)]
    AI --> Provider[Mock or External LLM Provider]
    Backend --> Mermaid[Mermaid Workflow Output]
```

## Document Generation Flow

```mermaid
sequenceDiagram
    participant Client as API Consumer
    participant Backend as Spring Boot Backend
    participant Service as Document Service
    participant Store as PostgreSQL
    participant AI as AI Service Boundary

    Client->>Backend: POST /api/v1/documents/generate
    Backend->>Backend: Validate title, type, context, tags
    Backend->>Service: Generate document
    Service->>Store: Create PENDING job
    Service->>Store: Mark job RUNNING
    Service->>AI: POST /v1/documents/generate
    Service->>Store: Save generated document
    Service->>Store: Mark job COMPLETED with resource ID
    Service-->>Backend: DocumentGenerationResponse with jobId
    Backend-->>Client: ApiResponse<DocumentGenerationResponse>
```

The backend calls the FastAPI AI service by default. Tests and offline demos can use deterministic local mode with `AI_SERVICE_MODE=local`.

## AI Pipeline Flow

```mermaid
flowchart TD
    Input[Structured or Semi-Structured Input] --> Query[Build Retrieval Query]
    Query --> Retrieve[Retrieve Context]
    Retrieve --> Render[Render Versioned Prompt]
    Render --> Provider[Mock or Configured LLM Provider]
    Provider --> Result[GenerationResult]
    Result --> Output[Document or Workflow Content]
```

## Persistence Overview

```mermaid
erDiagram
    DOCUMENT_GENERATION_JOBS ||--o| GENERATED_DOCUMENTS : creates
    DOCUMENT_GENERATION_JOBS ||--o| WORKFLOW_DIAGRAMS : creates

    DOCUMENT_GENERATION_JOBS {
        uuid id
        string status
        string resource_type
        string resource_id
        timestamp created_at
        timestamp updated_at
        text failure_message
    }

    GENERATED_DOCUMENTS {
        uuid id
        string title
        string document_type
        text content
        timestamp created_at
        timestamp updated_at
    }

    WORKFLOW_DIAGRAMS {
        uuid id
        string title
        text mermaid
        text warnings
        timestamp created_at
        timestamp updated_at
    }
```

The schema is intentionally generic and contains no seed data, client names, private prompts, or production identifiers.
