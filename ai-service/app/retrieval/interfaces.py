from typing import Protocol

from app.retrieval.types import DocumentChunk, RetrievalRequest, RetrievalResult


class EmbeddingProvider(Protocol):
    def embed(self, text: str) -> list[float]:
        raise NotImplementedError


class VectorStore(Protocol):
    def search(self, query_embedding: list[float], limit: int) -> list[DocumentChunk]:
        raise NotImplementedError


class Retriever(Protocol):
    def retrieve(self, request: RetrievalRequest) -> list[RetrievalResult]:
        raise NotImplementedError
