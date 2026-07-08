# FlowForge Backend

Spring Boot API for document generation, workflow diagrams, templates, generation jobs, and AI-service integration.

## Requirements

- Java 21
- Maven 3.9+ or a Maven wrapper
- PostgreSQL 16 for persistence-backed local runs

## Local Development

```bash
cd backend
mvn test
mvn spring-boot:run
```

The default configuration uses local PostgreSQL settings through environment variables. The demo security mode is disabled by default with `FLOWFORGE_SECURITY_ENABLED=false`.

## Docker

```bash
docker compose up --build
```

The compose file starts PostgreSQL and the backend with sample local credentials.

## API Docs

When the application is running:

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Main Endpoints

- `GET /api/v1/health`
- `POST /api/v1/documents/generate`
- `POST /api/v1/workflows/diagram`
- `GET /api/v1/generation-jobs/{jobId}`
- `GET /api/v1/templates`