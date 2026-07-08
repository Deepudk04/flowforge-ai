# Backend Architecture

The backend is a Spring Boot service under the `com.flowforge` package. It exposes thin REST controllers, delegates work to services, and keeps persistence models separate from API DTOs.

## Modules

- `common`: response envelopes, exceptions, correlation ID handling
- `config`: application properties, CORS, OpenAPI metadata
- `documents`: document generation API and service
- `workflows`: Mermaid workflow diagram API and service
- `jobs`: generation job API and service
- `templates`: template registry API and service
- `integration`: AI service client boundary and local adapter
- `persistence`: JPA entities and repositories
- `security`: local demo mode and JWT resource server skeleton
- `observability`: log masking helpers

## Request Flow

1. Controller validates the request DTO.
2. Service performs local orchestration or delegates to an integration boundary.
3. Responses are wrapped in `ApiResponse`.
4. Errors pass through `GlobalExceptionHandler`.
5. `CorrelationIdFilter` adds `X-Correlation-Id` to each response.

## Persistence

The first schema contains generation jobs, generated documents, and workflow diagrams. Table and column names are generic and contain no seed data.

## API Boundaries

Controllers return `ApiResponse<T>` envelopes and rely on DTO validation for request shape. The current generation services return deterministic local results, while `integration.AiServiceClient` defines the boundary for provider-backed generation.

## Error Handling

`GlobalExceptionHandler` maps validation, bad request, not found, and unexpected errors to a consistent response envelope. `CorrelationIdFilter` adds request correlation metadata to logs and responses.

## Security Profile

Local demo mode is disabled by default through `FLOWFORGE_SECURITY_ENABLED=false`. When enabled, the service is configured as a JWT resource server with generic roles and no committed issuer secrets.
