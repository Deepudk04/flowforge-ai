# Roadmap

## Near-Term Product Work

- Add async generation jobs with queue-backed processing.
- Add document export formats such as Markdown, PDF, and DOCX.
- Expand synthetic sample workflows for SOPs, compliance checklists, and process handbooks.
- Add prompt evaluation fixtures and regression examples.
- Add retrieval quality scoring for generated context.
- Add a generation history UI.

## Engineering Improvements

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


Executive Read

  This is a solid portfolio scaffold: clean repo layout, Spring Boot + FastAPI split, CI, docs, validation, tests, Docker Compose, security notes, and
  public-safe synthetic data. For an SDE-2 top-tech flagship project, the main weakness is that several advertised architecture pieces are not actually
  wired into runtime behavior yet. It currently reads more like a well-documented system skeleton than a production-grade service.

  Highest-Impact Improvements

  1. Wire the backend to the AI service
     Status: implemented. The Spring generation services now call the FastAPI AI layer through an HTTP client with timeout, retry/backoff,
     failure mapping, and correlation ID propagation. Deterministic local mode remains available through configuration.

     Key refs:
     backend/src/main/java/com/flowforge/integration/HttpAiServiceClient.java
     backend/src/main/java/com/flowforge/documents/service/DocumentGenerationService.java
     backend/src/main/java/com/flowforge/workflows/service/WorkflowDiagramService.java
     ai-service/app/generation_routes.py

     Follow-up:
      - Add fuller contract tests between the backend and AI service.
      - Consider circuit breaking once queue-backed generation is added.

  2. Make persistence real
     Status: implemented. Generation services now save generated documents, workflow diagrams, and generation jobs. Job lookup reads the
     repository, absent jobs return ResourceNotFoundException, and the schema includes created_at and updated_at coverage.

     Key refs:
     backend/src/main/java/com/flowforge/documents/service/DocumentGenerationService.java
     backend/src/main/java/com/flowforge/workflows/service/WorkflowDiagramService.java
     backend/src/main/java/com/flowforge/jobs/service/GenerationJobService.java
     backend/src/main/resources/db/migration/V2__add_generation_audit_columns.sql
     backend/src/test/java/com/flowforge/persistence/GenerationPersistencePostgresTest.java

     Follow-up:
      - Move longer generation work to queue-backed workers.
      - Add pagination/search for generated resource history.

  3. Reduce placeholder behavior
     Status: implemented. The deterministic mock paths now produce structured SOP-style sections, assumptions, risks, owners, and review
     checklists instead of plain prompt echoes. Workflow diagram generation continues to validate Mermaid node references.

     Key refs:
     backend/src/main/java/com/flowforge/integration/LocalAiServiceClient.java
     ai-service/app/providers.py
     ai-service/app/diagrams.py

     Follow-up:
      - Add richer synthetic samples for compliance checklists and process handbooks.
      - Add prompt evaluation fixtures for the structured output shape.

  4. Harden security
     Security is present, but not deep enough for a flagship backend.

     Key refs:
     backend/src/main/java/com/flowforge/security/SecurityConfig.java:46 maps roles, but does not enforce configured audience.
     backend/src/main/java/com/flowforge/security/SecurityConfig.java:48 defaults missing roles to USER, which is convenient but risky.
     backend/src/main/java/com/flowforge/observability/LogMasker.java:24 is case-sensitive, so Authorization will not be masked.

     Add:
      - JWT audience validator.
      - no default role in secured mode unless explicitly intended.
      - case-insensitive masking.
      - rate limiting for generation endpoints.
      - tests for secured mode, invalid audience, missing roles, and masked headers.

  5. Upgrade testing from unit-safe to system-confident
     Current tests mostly verify local deterministic behavior. Add:
      - Spring service tests with mocked AiServiceClient.
      - WireMock contract tests for backend -> AI service.
      - FastAPI route tests using TestClient.
      - Testcontainers PostgreSQL tests for Flyway + JPA.
      - failure-path tests: AI timeout, malformed AI response, vector DB failure.

  6. Add quality gates
     CI is simple and good, but top-tech reviewers like to see enforced standards.

     Key ref:
     .github/workflows/ci.yml:21

     Add:
      - Java: Checkstyle or Spotless, SpotBugs, JaCoCo coverage.
      - Python: ruff, mypy or pyright, coverage.
      - security: gitleaks, dependency review, Trivy image scan.
      - Maven wrapper, since mvn is assumed but not checked in.

  7. Improve API maturity
     Useful polish:
      - Add request/response examples via OpenAPI annotations.
      - Validate tags list size and normalize null to empty in backend/src/main/java/com/flowforge/documents/dto/DocumentDtos.java:16.
      - Use enums for document/job statuses instead of raw strings.
      - Add pagination/search for generation history.
      - Add export endpoints for Markdown/PDF/DOCX if you want a standout feature.

  Priority Order

  1. Contract/integration tests.
  2. Security hardening.
  3. CI/static analysis/security scans.
  4. RAG schema and retrieval quality scoring.
  5. Export/history features.

  Verification Note

  I attempted fresh local checks, but this sandbox does not have python, pytest, or mvn on PATH, so I could not run the test suites here. Existing repo
  artifacts suggest prior Maven tests were run, but I would not present this as freshly verified without rerunning locally or in CI.
