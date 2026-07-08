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
def ready() -> dict[str, str | bool | int]:
    settings = get_settings()
    return {
        "status": "ready",
        "llmProvider": settings.llm_provider,
        "llmConfigured": settings.remote_llm_configured,
        "defaultModel": settings.default_model,
        "embeddingProvider": settings.embedding_provider,
        "embeddingModel": settings.embedding_model,
        "retrievalBackend": settings.retrieval_backend,
        "retrievalDefaultLimit": settings.retrieval_default_limit,
        "vectorStoreConfigured": settings.vector_store_configured,
    }
