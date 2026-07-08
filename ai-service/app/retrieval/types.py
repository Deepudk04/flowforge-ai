from pydantic import BaseModel, Field


class DocumentChunk(BaseModel):
    chunk_id: str
    content: str
    title: str = "Untitled"
    metadata: dict[str, str] = Field(default_factory=dict)


class RetrievalRequest(BaseModel):
    query: str
    limit: int = Field(default=5, ge=1, le=50)
    filters: dict[str, str] = Field(default_factory=dict)


class RetrievalResult(BaseModel):
    chunk: DocumentChunk
    score: float = Field(ge=0.0, le=1.0)
