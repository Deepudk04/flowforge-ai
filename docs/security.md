# Security Notes

FlowForge services run with local mock/demo defaults unless configured otherwise. Production deployments should provide secrets through environment variables or a managed secret store.

## Public Repository Controls

- No real provider keys, database passwords, private prompts, client documents, or production configuration should be committed.
- `.env` files stay local and are ignored.
- Example credentials such as `flowforge` are local-only placeholders for Docker Compose and documentation.
- Sample inputs and outputs must remain synthetic and generic.

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

## Prompt and Data Sanitization

- Do not include proprietary process text in committed prompt examples.
- Keep retrieval examples small, generic, and synthetic.
- Treat generated output as untrusted text until reviewed by a human.
- Avoid logging raw request bodies when integrating real providers.

## Recommended Scans

Use gitleaks when available:

```bash
gitleaks detect --source .
```

Fallback keyword scan:

```bash
grep -RniE "password|passwd|secret|token|api[_-]?key|apikey|client_secret|private_key|BEGIN RSA|BEGIN PRIVATE|jwt|bearer|authorization|jdbc:|mongodb|postgresql://|mysql://|redis://|openai|anthropic|gemini|azure|aws_access_key|aws_secret" . --exclude-dir=.git --exclude-dir=target --exclude-dir=.venv --exclude-dir=venv
```
