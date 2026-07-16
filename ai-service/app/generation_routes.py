from fastapi import APIRouter
from pydantic import BaseModel, ConfigDict, Field

from app.diagrams import WorkflowDiagramGenerator, WorkflowDiagramRequest, WorkflowEdge, WorkflowNode
from app.models import DocumentGenerationRequest
from app.pipeline import DocumentGenerationPipeline

router = APIRouter(prefix="/v1")


class BackendDocumentGenerationRequest(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    document_type: str = Field(alias="documentType", min_length=1, max_length=80)
    input_context: str = Field(alias="inputContext", min_length=1, max_length=12000)
    tags: list[str] = Field(default_factory=list)

    model_config = ConfigDict(populate_by_name=True)


class BackendDocumentGenerationResponse(BaseModel):
    content: str
    model: str
    retrievedSourceIds: list[str] = Field(default_factory=list)


class BackendWorkflowStepRequest(BaseModel):
    id: str = Field(min_length=1, max_length=80)
    label: str = Field(min_length=1, max_length=120)
    next_step_id: str | None = Field(default=None, alias="nextStepId", max_length=80)

    model_config = ConfigDict(populate_by_name=True)


class BackendWorkflowDiagramRequest(BaseModel):
    title: str = Field(min_length=1, max_length=160)
    steps: list[BackendWorkflowStepRequest] = Field(min_length=1)


class BackendWorkflowDiagramResponse(BaseModel):
    mermaid: str
    warnings: list[str] = Field(default_factory=list)


@router.post("/documents/generate", response_model=BackendDocumentGenerationResponse)
def generate_document(request: BackendDocumentGenerationRequest) -> BackendDocumentGenerationResponse:
    result = DocumentGenerationPipeline().run(
        DocumentGenerationRequest(
            document_id=request.title,
            objective=f"{request.document_type}: {request.title}",
            source_text=request.input_context,
        )
    )
    return BackendDocumentGenerationResponse(
        content=result.content,
        model=result.model,
        retrievedSourceIds=result.retrieval_context_used,
    )


@router.post("/workflows/diagram", response_model=BackendWorkflowDiagramResponse)
def generate_workflow_diagram(request: BackendWorkflowDiagramRequest) -> BackendWorkflowDiagramResponse:
    nodes = [WorkflowNode(node_id=step.id, label=step.label) for step in request.steps]
    edges = [
        WorkflowEdge(from_node_id=step.id, to_node_id=step.next_step_id)
        for step in request.steps
        if step.next_step_id
    ]
    result = WorkflowDiagramGenerator().generate(
        WorkflowDiagramRequest(title=request.title, nodes=nodes, edges=edges)
    )
    return BackendWorkflowDiagramResponse(mermaid=result.mermaid, warnings=result.warnings)
