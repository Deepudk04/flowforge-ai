from fastapi.testclient import TestClient

from app.main import create_app


def test_health_returns_public_service_metadata():
    client = TestClient(create_app())

    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {
        "status": "ok",
        "service": "FlowForge AI Service",
        "environment": "local",
    }


def test_ready_uses_public_safe_defaults():
    client = TestClient(create_app())

    response = client.get("/ready")

    assert response.status_code == 200
    body = response.json()
    assert body["llmProvider"] == "mock"
    assert body["llmConfigured"] is True
    assert body["defaultModel"] == "flowforge-local-mock"
    assert body["embeddingProvider"] == "mock"
    assert body["embeddingModel"] == "flowforge-local-embedding"
    assert body["retrievalBackend"] == "memory"
    assert body["retrievalDefaultLimit"] == 5
    assert body["vectorStoreConfigured"] is True
