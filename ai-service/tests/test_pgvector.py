from app.config import Settings
from app.retrieval import PgVectorRetriever, RetrievalRequest


class FakeCursor:
    def __init__(self):
        self.params = None

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, traceback):
        return False

    def execute(self, query, params):
        self.params = params

    def fetchall(self):
        return [("chunk-1", "Guide", "Use clear sections.", {"kind": "document"}, 0.87)]


class FakeConnection:
    def __init__(self):
        self.cursor_instance = FakeCursor()
        self.registered = False

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, traceback):
        return False

    def cursor(self):
        return self.cursor_instance


def test_pgvector_retriever_maps_rows_to_results():
    connection = FakeConnection()
    settings = Settings(vector_db_url="local-vector-store")
    retriever = PgVectorRetriever(
        settings=settings,
        connection_factory=lambda _: connection,
        register_vector_fn=lambda conn: setattr(conn, "registered", True),
    )

    results = retriever.retrieve(RetrievalRequest(query="clear sections", limit=3))

    assert connection.registered is True
    assert len(results) == 1
    assert results[0].chunk.chunk_id == "chunk-1"
    assert results[0].chunk.metadata == {"kind": "document"}
    assert results[0].score == 0.87
