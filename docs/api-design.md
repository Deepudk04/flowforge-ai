# Backend API Design

The backend uses `/api/v1` routes and JSON request/response bodies. Responses are wrapped in a standard envelope:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

OpenAPI metadata is available when the backend is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## POST /api/v1/documents/generate

Generates a structured process document from a title, document type, and source context.

Request:

```json
{
  "title": "Vendor Approval SOP",
  "documentType": "standard-operating-procedure",
  "inputContext": "A requester submits a vendor intake form. Procurement reviews risk, finance validates payment details, and legal reviews contract terms before approval.",
  "tags": ["vendor", "procurement", "approval"]
}
```

Success response:

```json
{
  "success": true,
  "data": {
    "documentId": "doc_00000000-0000-0000-0000-000000000000",
    "title": "Vendor Approval SOP",
    "documentType": "standard-operating-procedure",
    "content": "# Vendor Approval SOP\n\nA requester submits a vendor intake form. Procurement reviews risk, finance validates payment details, and legal reviews contract terms before approval.",
    "status": "COMPLETED",
    "createdAt": "2026-07-09T00:00:00Z",
    "jobId": "job_00000000-0000-0000-0000-000000000000"
  },
  "error": null
}
```

## GET /api/v1/documents/{documentId}

Returns a persisted generated document and its associated generation job status.

Success response:

```json
{
  "success": true,
  "data": {
    "documentId": "doc_00000000-0000-0000-0000-000000000000",
    "title": "Vendor Approval SOP",
    "documentType": "standard-operating-procedure",
    "content": "# Vendor Approval SOP\n\n...",
    "status": "COMPLETED",
    "createdAt": "2026-07-09T00:00:00Z",
    "jobId": "job_00000000-0000-0000-0000-000000000000"
  },
  "error": null
}
```

Validation failure response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "path": "/api/v1/documents/generate",
    "correlationId": "request-correlation-id",
    "timestamp": "2026-07-09T00:00:00Z",
    "validationErrors": [
      {
        "field": "title",
        "message": "must not be blank"
      }
    ]
  }
}
```

## POST /api/v1/workflows/diagram

Generates a Mermaid workflow diagram from a list of steps and optional next-step links.

Request:

```json
{
  "title": "Vendor Approval Workflow",
  "steps": [
    {
      "id": "intake",
      "label": "Submit vendor intake form",
      "nextStepId": "procurement_review"
    },
    {
      "id": "procurement_review",
      "label": "Procurement reviews risk",
      "nextStepId": "finance_review"
    },
    {
      "id": "finance_review",
      "label": "Finance validates payment details",
      "nextStepId": "approval"
    },
    {
      "id": "approval",
      "label": "Approve vendor"
    }
  ]
}
```

Success response:

```json
{
  "success": true,
  "data": {
    "workflowId": "workflow_00000000-0000-0000-0000-000000000000",
    "title": "Vendor Approval Workflow",
    "mermaid": "flowchart TD\n    intake[Submit vendor intake form]\n    procurement_review[Procurement reviews risk]\n    finance_review[Finance validates payment details]\n    approval[Approve vendor]\n    intake --> procurement_review\n    procurement_review --> finance_review\n    finance_review --> approval",
    "warnings": [],
    "status": "COMPLETED",
    "createdAt": "2026-07-09T00:00:00Z",
    "jobId": "job_00000000-0000-0000-0000-000000000000"
  },
  "error": null
}
```

## GET /api/v1/workflows/{workflowId}

Returns a persisted workflow diagram and its associated generation job status.

Success response:

```json
{
  "success": true,
  "data": {
    "workflowId": "workflow_00000000-0000-0000-0000-000000000000",
    "title": "Vendor Approval Workflow",
    "mermaid": "flowchart TD\n    intake[Submit vendor intake form]",
    "warnings": [],
    "status": "COMPLETED",
    "createdAt": "2026-07-09T00:00:00Z",
    "jobId": "job_00000000-0000-0000-0000-000000000000"
  },
  "error": null
}
```

Failure response for an empty step list:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "path": "/api/v1/workflows/diagram",
    "correlationId": "request-correlation-id",
    "timestamp": "2026-07-09T00:00:00Z",
    "validationErrors": [
      {
        "field": "steps",
        "message": "must not be empty"
      }
    ]
  }
}
```

## GET /api/v1/generation-jobs/{jobId}

Returns generation job metadata for a supplied job ID.

Success response:

```json
{
  "success": true,
  "data": {
    "jobId": "job_demo_vendor_approval",
    "status": "COMPLETED",
    "resourceType": "document",
    "resourceId": "doc_00000000-0000-0000-0000-000000000000",
    "createdAt": "2026-07-09T00:00:00Z",
    "updatedAt": "2026-07-09T00:00:00Z"
  },
  "error": null
}
```

## GET /api/v1/templates

Lists available public-safe template metadata.

Success response:

```json
{
  "success": true,
  "data": [
    {
      "templateId": "template-document-v1",
      "name": "Document draft",
      "documentType": "document",
      "version": "v1",
      "tags": ["document"]
    },
    {
      "templateId": "template-workflow-v1",
      "name": "Workflow diagram",
      "documentType": "workflow",
      "version": "v1",
      "tags": ["workflow"]
    }
  ],
  "error": null
}
```
