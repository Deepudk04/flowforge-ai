import hashlib
from collections.abc import Callable
from typing import Any

from app.config import Settings, get_settings
from app.core.errors import ProviderConfigurationError, ProviderExecutionError
from app.retrieval.types import DocumentChunk, RetrievalRequest, RetrievalResult

ConnectionFactory = Callable[[str], Any]
RegisterVector = Callable[[Any], None]


class HashEmbeddingProvider:
    def __init__(self, dimensions: int):
        self.dimensions = dimensions

    def embed(self, text: str) -> list[float]:
        vector = [0.0] * self.dimensions
        terms = [term.lower() for term in text.split() if term.strip()]
        for term in terms:
            digest = hashlib.sha256(term.encode("utf-8")).digest()
            index = int.from_bytes(digest[:4], "big") % self.dimensions
            sign = 1.0 if digest[4] % 2 == 0 else -1.0
            vector[index] += sign
        length = sum(value * value for value in vector) ** 0.5
        return [value / length for value in vector] if length else vector


class PgVectorRetriever:
    def __init__(
        self,
        settings: Settings | None = None,
        embedding_provider: HashEmbeddingProvider | None = None,
        connection_factory: ConnectionFactory | None = None,
        register_vector_fn: RegisterVector | None = None,
    ):
        self.settings = settings or get_settings()
        if not self.settings.vector_db_url:
            raise ProviderConfigurationError("VECTOR_DB_URL is required when RETRIEVAL_BACKEND=pgvector.")
        self.embedding_provider = embedding_provider or HashEmbeddingProvider(self.settings.embedding_dimensions)
        self.connection_factory = connection_factory or self._default_connection_factory()
        self.register_vector_fn = register_vector_fn or self._default_register_vector()

    def retrieve(self, request: RetrievalRequest) -> list[RetrievalResult]:
        query_embedding = self.embedding_provider.embed(request.query)
        try:
            with self.connection_factory(self.settings.vector_db_url) as connection:
                self.register_vector_fn(connection)
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        select chunk_id, title, content, metadata, score
                        from document_chunks
                        where embedding is not null
                        order by embedding <=> %s::vector
                        limit %s
                        """,
                        (query_embedding, request.limit),
                    )
                    rows = cursor.fetchall()
        except Exception as exc:
            raise ProviderExecutionError("pgvector retrieval failed.") from exc

        return [self._row_to_result(row) for row in rows]

    def _row_to_result(self, row: tuple[Any, ...]) -> RetrievalResult:
        metadata = row[3] or {}
        return RetrievalResult(
            chunk=DocumentChunk(
                chunk_id=str(row[0]),
                title=str(row[1] or "Untitled"),
                content=str(row[2] or ""),
                metadata={str(key): str(value) for key, value in metadata.items()},
            ),
            score=max(0.0, min(1.0, float(row[4]))),
        )

    def _default_connection_factory(self) -> ConnectionFactory:
        try:
            import psycopg
        except ImportError as exc:
            raise ProviderConfigurationError("Install psycopg to use pgvector retrieval.") from exc
        return psycopg.connect

    def _default_register_vector(self) -> RegisterVector:
        try:
            from pgvector.psycopg import register_vector
        except ImportError as exc:
            raise ProviderConfigurationError("Install pgvector to use pgvector retrieval.") from exc
        return register_vector
