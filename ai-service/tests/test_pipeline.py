from app.models import DocumentGenerationRequest
from app.pipeline import DocumentGenerationPipeline
from app.retrieval import DocumentChunk, InMemoryRetriever


def test_document_generation_pipeline_adds_retrieved_context():
    retriever = InMemoryRetriever(
        [
            DocumentChunk(
                chunk_id="guide-1",
                title="Onboarding guide",
                content="Onboarding drafts should list assumptions and next steps.",
            )
        ]
    )
    pipeline = DocumentGenerationPipeline(retriever=retriever)

    result = pipeline.run(
        DocumentGenerationRequest(
            client_name="DemoClient",
            objective="Prepare onboarding notes.",
            source_text="Collect intake information and assign a reviewer.",
        )
    )

    assert result.kind == "document"
    assert result.retrieval_context_used == ["guide-1"]
    assert "DemoClient" in result.content
    assert "Onboarding drafts should list assumptions" in result.content
