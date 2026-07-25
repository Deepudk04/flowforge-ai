from fastapi import APIRouter

from app.config import get_settings
from app.providers import create_provider
from app.retrieval import create_retriever

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
    provider = create_provider(settings)
    retriever = create_retriever(settings)
    return {
        "status": "ready",
        "llmProvider": provider.name,
        "model": provider.model,
        "embeddingProvider": settings.embedding_provider,
        "embeddingModel": settings.embedding_model,
        "retrievalBackend": retriever.name,
        "retrievalDefaultLimit": settings.retrieval_default_limit,
        "vectorStoreConfigured": settings.vector_store_configured,
    }
