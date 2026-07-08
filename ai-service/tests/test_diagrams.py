from app.diagrams import WorkflowDiagramGenerator, WorkflowDiagramRequest, WorkflowEdge, WorkflowNode


def test_workflow_diagram_generator_outputs_mermaid():
    generator = WorkflowDiagramGenerator()

    result = generator.generate(
        WorkflowDiagramRequest(
            title="SampleWorkflow",
            nodes=[
                WorkflowNode(node_id="intake", label="Receive intake"),
                WorkflowNode(node_id="review", label="Review request"),
                WorkflowNode(node_id="draft", label="Prepare draft"),
            ],
            edges=[
                WorkflowEdge(from_node_id="intake", to_node_id="review", label="next"),
                WorkflowEdge(from_node_id="review", to_node_id="draft"),
            ],
        )
    )

    assert result.title == "SampleWorkflow"
    assert result.warnings == []
    assert result.mermaid.splitlines()[0] == "flowchart TD"
    assert "intake[Receive intake]" in result.mermaid
    assert "intake -->|next| review" in result.mermaid


def test_workflow_diagram_generator_skips_unknown_edges():
    generator = WorkflowDiagramGenerator()

    result = generator.generate(
        WorkflowDiagramRequest(
            nodes=[WorkflowNode(node_id="start", label="Start")],
            edges=[WorkflowEdge(from_node_id="start", to_node_id="missing")],
        )
    )

    assert "missing" not in result.mermaid
    assert result.warnings == ["Skipping edge to unknown node: missing"]
