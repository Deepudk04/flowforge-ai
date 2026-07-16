from fastapi.testclient import TestClient

from app.main import create_app


def test_document_generation_route_returns_backend_contract():
    client = TestClient(create_app())

    response = client.post(
        "/v1/documents/generate",
        json={
            "title": "SampleDocument",
            "documentType": "document",
            "inputContext": "DemoClient provided input.",
            "tags": ["sample"],
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["model"] == "flowforge-local-mock"
    assert "SampleDocument" in body["content"]
    assert "DemoClient provided input." in body["content"]
    assert body["retrievedSourceIds"] == ["sample-document-guide"]


def test_workflow_diagram_route_returns_mermaid():
    client = TestClient(create_app())

    response = client.post(
        "/v1/workflows/diagram",
        json={
            "title": "Vendor Approval Workflow",
            "steps": [
                {
                    "id": "intake",
                    "label": "Submit vendor intake form",
                    "nextStepId": "approval",
                },
                {
                    "id": "approval",
                    "label": "Approve vendor",
                },
            ],
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["warnings"] == []
    assert "flowchart TD" in body["mermaid"]
    assert "intake --> approval" in body["mermaid"]


def test_workflow_diagram_route_reports_unknown_next_step():
    client = TestClient(create_app())

    response = client.post(
        "/v1/workflows/diagram",
        json={
            "title": "Broken Workflow",
            "steps": [
                {
                    "id": "intake",
                    "label": "Submit vendor intake form",
                    "nextStepId": "missing",
                }
            ],
        },
    )

    assert response.status_code == 200
    assert response.json()["warnings"] == ["Skipping edge to unknown node: missing"]
