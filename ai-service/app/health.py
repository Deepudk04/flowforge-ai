from fastapi import APIRouter

from app.config import get_settings

router = APIRouter()


@router.get("/health")
def health() -> dict[str, str]:
    settings = get_settings()
    return {
        "status": "ok",
        "service": settings.service_name,
        "environment": settings.environment,
    }


@router.get("/ready")
def ready() -> dict[str, str | bool]:
    settings = get_settings()
    return {
        "status": "ready",
        "providerConfigured": settings.llm_provider == "mock",
        "defaultModel": settings.default_model,
    }
