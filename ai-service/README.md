# FlowForge AI Service

This service contains the public-safe AI layer for FlowForge. It starts with a minimal FastAPI skeleton and a deterministic local configuration path so the project can run without real provider credentials.

## Local Setup

```bash
cd ai-service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
uvicorn app.main:app --reload
```

## Endpoints

- `GET /health` returns service liveness metadata.
- `GET /ready` returns basic readiness metadata for local development.

No real LLM keys, private prompts, customer data, or production configuration are included in this skeleton.
