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


def test_ready_uses_mock_provider_by_default():
    client = TestClient(create_app())

    response = client.get("/ready")

    assert response.status_code == 200
    assert response.json()["providerConfigured"] is True
    assert response.json()["defaultModel"] == "flowforge-local-mock"
