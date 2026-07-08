from app.retrieval import DocumentChunk, InMemoryRetriever, RetrievalRequest


def test_in_memory_retriever_returns_ranked_matches():
    retriever = InMemoryRetriever(
        [
            DocumentChunk(
                chunk_id="chunk-1",
                title="Onboarding guide",
                content="Collect intake details and validate the request.",
                metadata={"type": "guide"},
            ),
            DocumentChunk(
                chunk_id="chunk-2",
                title="Invoice note",
                content="Record billing information.",
                metadata={"type": "note"},
            ),
        ]
    )

    results = retriever.retrieve(RetrievalRequest(query="intake request", limit=1))

    assert len(results) == 1
    assert results[0].chunk.chunk_id == "chunk-1"
    assert results[0].score > 0


def test_in_memory_retriever_applies_metadata_filters():
    retriever = InMemoryRetriever(
        [
            DocumentChunk(chunk_id="a", content="Sample workflow context", metadata={"kind": "workflow"}),
            DocumentChunk(chunk_id="b", content="Sample document context", metadata={"kind": "document"}),
        ]
    )

    results = retriever.retrieve(RetrievalRequest(query="sample context", filters={"kind": "document"}))

    assert [result.chunk.chunk_id for result in results] == ["b"]
