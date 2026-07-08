from app.retrieval.types import DocumentChunk, RetrievalRequest, RetrievalResult


class InMemoryRetriever:
    def __init__(self, chunks: list[DocumentChunk] | None = None):
        self.chunks = chunks or []

    def retrieve(self, request: RetrievalRequest) -> list[RetrievalResult]:
        query_terms = self._terms(request.query)
        ranked: list[RetrievalResult] = []
        for chunk in self.chunks:
            if not self._matches_filters(chunk, request.filters):
                continue
            score = self._score(query_terms, chunk)
            if score > 0:
                ranked.append(RetrievalResult(chunk=chunk, score=score))
        ranked.sort(key=lambda result: result.score, reverse=True)
        return ranked[: request.limit]

    def _matches_filters(self, chunk: DocumentChunk, filters: dict[str, str]) -> bool:
        return all(chunk.metadata.get(key) == value for key, value in filters.items())

    def _score(self, query_terms: set[str], chunk: DocumentChunk) -> float:
        if not query_terms:
            return 0.0
        chunk_terms = self._terms(f"{chunk.title} {chunk.content}")
        overlap = len(query_terms & chunk_terms)
        return min(1.0, overlap / len(query_terms))

    def _terms(self, text: str) -> set[str]:
        return {term.strip(".,:;!?()[]{}\"'").lower() for term in text.split() if term.strip()}
