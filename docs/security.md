# Security Notes

FlowForge services run with local mock/demo defaults unless configured otherwise. Production deployments should provide secrets through environment variables or a managed secret store.

## Backend

- Keep `.env` files local.
- Commit example config only.
- Use `FLOWFORGE_SECURITY_ENABLED=true` to enable JWT resource-server checks.
- Use generic roles: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_VIEWER`.
- Configure `JWT_ISSUER_URI` and `JWT_AUDIENCE` through the environment.
- Do not log request bodies or provider credentials.

## Logging

Sensitive keys should be masked before logging:

- `Authorization`
- `Cookie`
- `Set-Cookie`
- `password`
- `token`
- `apiKey`
- `secret`

## AI Service

The AI service uses mock providers by default. Add real provider credentials only through runtime configuration.