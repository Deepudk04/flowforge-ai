from typing import Literal

from pydantic import BaseModel, Field

GenerationKind = Literal["document", "workflow"]


class RetrievalContext(BaseModel):
    source_id: str
    title: str
    content: str
    score: float = Field(default=1.0, ge=0.0, le=1.0)


class DocumentGenerationRequest(BaseModel):
    document_id: str = "SampleDocument"
    client_name: str = "DemoClient"
    objective: str
    source_text: str
    retrieval_context: list[RetrievalContext] = Field(default_factory=list)


class WorkflowGenerationRequest(BaseModel):
    workflow_id: str = "SampleWorkflow"
    title: str
    steps: list[str] = Field(min_length=1)
    retrieval_context: list[RetrievalContext] = Field(default_factory=list)


class UsageTrace(BaseModel):
    input_tokens: int = 0
    output_tokens: int = 0
    provider: str
    model: str


class GenerationResult(BaseModel):
    kind: GenerationKind
    content: str
    model: str
    usage: UsageTrace
    retrieval_context_used: list[str] = Field(default_factory=list)
