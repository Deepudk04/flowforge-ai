from app.config import Settings
from app.retrieval.interfaces import Retriever
from app.retrieval.memory import InMemoryRetriever
from app.retrieval.pgvector import PgVectorRetriever
from app.retrieval.types import DocumentChunk


def create_retriever(
    settings: Settings,
    memory_chunks: list[DocumentChunk] | None = None,
) -> Retriever:
    if settings.retrieval_backend == "memory":
        return InMemoryRetriever(memory_chunks)
    if settings.retrieval_backend == "pgvector":
        return PgVectorRetriever(settings)
    raise ValueError(f"Unsupported retrieval backend: {settings.retrieval_backend}")
