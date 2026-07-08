# FlowForge AI Service

This service contains the public-safe AI layer for FlowForge. It starts with a minimal FastAPI skeleton and a deterministic local configuration path so the project can run without real provider credentials.

## Local Setup

```bash
cd ai-service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
python -m compileall app tests
pytest -v
uvicorn app.main:app --reload
```

## Docker

From the repository root:

```bash
docker compose up --build ai-service
```

## Configuration

All runtime configuration is provided through environment variables. Local development uses mock providers by default, so no real LLM or embedding credentials are required.

| Variable | Purpose | Safe default |
| --- | --- | --- |
| `SERVICE_NAME` | Public service display name | `FlowForge AI Service` |
| `ENVIRONMENT` | Runtime environment label | `local` |
| `LOG_LEVEL` | Application log level | `INFO` |
| `LLM_PROVIDER` | LLM provider selector | `mock` |
| `LLM_API_KEY` | Optional provider credential | empty |
| `DEFAULT_MODEL` | Default generation model | `flowforge-local-mock` |
| `EMBEDDING_PROVIDER` | Embedding provider selector | `mock` |
| `EMBEDDING_MODEL` | Default embedding model | `flowforge-local-embedding` |
| `RETRIEVAL_BACKEND` | Retrieval backend selector | `memory` |
| `VECTOR_DB_URL` | Optional vector store URL | empty |

## Endpoints

- `GET /health` returns service liveness metadata.
- `GET /ready` returns non-sensitive readiness metadata for local development.

No real LLM keys, private prompts, customer data, or production configuration are included in this skeleton.
