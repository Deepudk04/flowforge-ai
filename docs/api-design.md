# Backend API Design

The backend uses `/api/v1` routes and JSON request/response bodies. Responses are wrapped in a standard envelope:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

## Documents

`POST /api/v1/documents/generate`

```json
{
  "title": "SampleDocument",
  "documentType": "document",
  "inputContext": "DemoClient provided input.",
  "tags": ["sample"]
}
```

## Workflows

`POST /api/v1/workflows/diagram`

```json
{
  "title": "SampleWorkflow",
  "steps": [
    { "id": "intake", "label": "Receive intake", "nextStepId": "review" },
    { "id": "review", "label": "Review request" }
  ]
}
```

## Jobs and Templates

- `GET /api/v1/generation-jobs/{jobId}`
- `GET /api/v1/templates`

OpenAPI metadata is available from `/v3/api-docs` when the app is running.