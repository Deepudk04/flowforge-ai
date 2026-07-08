# Security Notes

FlowForge AI is designed to run with mock providers by default. Production deployments should provide secrets through the runtime environment or a managed secret store.

## Checks

Recommended local checks before committing:

```bash
git diff --check
gitleaks detect --source .
pytest
pip-audit
```

If a tool is unavailable, document that in the review notes and run the remaining checks.
