# Local Development

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

## Docker Compose

```bash
docker compose up --build
```

The local compose file uses sample PostgreSQL credentials and does not include production settings.

## Checks

Run these before committing when tools are available:

```bash
git diff --check
gitleaks detect --source .
cd backend && mvn test
```