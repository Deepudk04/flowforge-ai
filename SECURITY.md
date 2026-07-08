# Security Policy

## Reporting

Please report security issues privately through the repository owner. Do not open a public issue for suspected vulnerabilities.

## Configuration

- Keep `.env` files local.
- Commit `.env.example` only.
- Pass provider keys, database URLs, and deployment settings through environment variables.
- Rotate any credential that is accidentally exposed.

## Data Handling

Use sample data for demos and tests. Do not commit customer documents, logs, database exports, screenshots, or provider responses that contain private information.
