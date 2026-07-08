# FlowForge Backend

Spring Boot API for FlowForge document and workflow generation.

## Local Development

```bash
cd backend
mvn test
mvn spring-boot:run
```

The backend starts with a minimal health endpoint and safe local configuration. Runtime values should be supplied through environment variables as the service grows.

## Endpoints

- `GET /api/v1/health` returns basic service metadata.
- `GET /actuator/health` is provided by Spring Boot Actuator.