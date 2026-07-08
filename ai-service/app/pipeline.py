from app.config import Settings, get_settings
from app.models import DocumentGenerationRequest, GenerationResult
from app.orchestrator import AIOrchestrator
from app.retrieval import DocumentChunk, InMemoryRetriever, RetrievalRequest


class DocumentGenerationPipeline:
    def __init__(
        self,
        settings: Settings | None = None,
        retriever: InMemoryRetriever | None = None,
        orchestrator: AIOrchestrator | None = None,
    ):
        self.settings = settings or get_settings()
        self.retriever = retriever or InMemoryRetriever(self._default_chunks())
        self.orchestrator = orchestrator or AIOrchestrator(self.settings)

    def run(self, request: DocumentGenerationRequest) -> GenerationResult:
        retrieval_results = self.retriever.retrieve(
            RetrievalRequest(
                query=f"{request.objective} {request.source_text}",
                limit=self.settings.retrieval_default_limit,
            )
        )
        enriched = request.model_copy(
            update={
                "retrieval_context": [
                    {
                        "source_id": result.chunk.chunk_id,
                        "title": result.chunk.title,
                        "content": result.chunk.content,
                        "score": result.score,
                    }
                    for result in retrieval_results
                ]
            }
        )
        return self.orchestrator.generate_document(enriched)

    def _default_chunks(self) -> list[DocumentChunk]:
        return [
            DocumentChunk(
                chunk_id="sample-document-guide",
                title="Document drafting guide",
                content="Use short sections, include assumptions, and cite only provided context.",
                metadata={"kind": "document"},
            )
        ]
