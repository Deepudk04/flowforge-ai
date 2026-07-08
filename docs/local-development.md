# Local Development

## Requirements

- Java 21
- Maven 3.9+
- Python 3.12
- Docker Desktop, if using Compose

## Backend

```bash
cd backend
mvn test
mvn spring-boot:run
```

Useful environment variables:

```text
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://localhost:5432/flowforge
DATABASE_USERNAME=flowforge
DATABASE_PASSWORD=flowforge
AI_SERVICE_BASE_URL=http://localhost:8000
FLOWFORGE_SECURITY_ENABLED=false
JWT_AUDIENCE=flowforge-api
```

The same safe placeholder values are available in `backend/.env.example`.

## Docker Compose

```bash
docker compose up --build
```

The local compose file starts PostgreSQL, the FastAPI AI service, and the Spring Boot backend. It uses sample PostgreSQL credentials and mock AI settings only.

## AI Service

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

Useful local endpoints:

- Backend Swagger UI: `http://localhost:8080/swagger-ui.html`
- Backend health: `http://localhost:8080/api/v1/health`
- AI service health: `http://localhost:8000/health`
- AI service readiness: `http://localhost:8000/ready`

## Sample Requests

```bash
curl -X POST http://localhost:8080/api/v1/documents/generate ^
  -H "Content-Type: application/json" ^
  --data @samples/input/process-document-request.sample.json
```

```bash
curl -X POST http://localhost:8080/api/v1/workflows/diagram ^
  -H "Content-Type: application/json" ^
  --data @samples/input/workflow-diagram-request.sample.json
```

## Checks

Run these before committing when tools are available:

```bash
git diff --check
gitleaks detect --source .
cd backend && mvn test
cd ai-service && python -m compileall app tests && pytest -v
```
