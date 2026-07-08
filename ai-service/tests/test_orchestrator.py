from app.models import DocumentGenerationRequest, RetrievalContext, WorkflowGenerationRequest
from app.orchestrator import AIOrchestrator


def test_orchestrator_generates_document_with_mock_provider():
    orchestrator = AIOrchestrator()

    result = orchestrator.generate_document(
        DocumentGenerationRequest(
            objective="Summarize an onboarding process.",
            source_text="DemoClient submits a sample intake form for review.",
            retrieval_context=[
                RetrievalContext(
                    source_id="sample-context-1",
                    title="Sample policy",
                    content="Use concise and factual language.",
                )
            ],
        )
    )

    assert result.kind == "document"
    assert result.model == "flowforge-local-mock"
    assert "DemoClient" in result.content
    assert result.retrieval_context_used == ["sample-context-1"]
    assert result.usage.provider == "mock"


def test_orchestrator_generates_workflow_with_mock_provider():
    orchestrator = AIOrchestrator()

    result = orchestrator.generate_workflow(
        WorkflowGenerationRequest(
            title="SampleWorkflow",
            steps=["Receive sample input", "Validate fields", "Generate draft"],
        )
    )

    assert result.kind == "workflow"
    assert "SampleWorkflow" in result.content
    assert "1. Receive sample input" in result.content
    assert result.retrieval_context_used == []
