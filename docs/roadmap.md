# Roadmap

## Near-Term Product Work

- Wire backend generation services to the FastAPI AI service through a resilient HTTP client.
- Add async generation jobs with queue-backed processing.
- Add document export formats such as Markdown, PDF, and DOCX.
- Expand synthetic sample workflows for SOPs, compliance checklists, and process handbooks.
- Add prompt evaluation fixtures and regression examples.
- Add retrieval quality scoring for generated context.
- Add a generation history UI.

## Engineering Improvements

- Add Testcontainers integration for PostgreSQL.
- Add queue-based generation and retry handling.
- Add response caching for repeated synthetic demo requests.
- Add rate limiting and stricter auth profiles.
- Add structured audit events for generation requests.
- Add observability dashboards and service-level metrics.
- Add contract tests between the backend and AI service.

## Public Version Guardrails

- Keep examples synthetic.
- Do not commit provider credentials.
- Do not claim production usage.
- Keep roadmap items separate from implemented features.
