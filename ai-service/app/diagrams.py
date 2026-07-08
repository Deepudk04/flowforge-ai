from pydantic import BaseModel, Field


class WorkflowNode(BaseModel):
    node_id: str
    label: str


class WorkflowEdge(BaseModel):
    from_node_id: str
    to_node_id: str
    label: str | None = None


class WorkflowDiagramRequest(BaseModel):
    title: str = "SampleWorkflow"
    nodes: list[WorkflowNode] = Field(min_length=1)
    edges: list[WorkflowEdge] = Field(default_factory=list)


class WorkflowDiagramResult(BaseModel):
    title: str
    mermaid: str
    warnings: list[str] = Field(default_factory=list)


class WorkflowDiagramGenerator:
    def generate(self, request: WorkflowDiagramRequest) -> WorkflowDiagramResult:
        node_ids = {node.node_id for node in request.nodes}
        warnings = self._validate_edges(request.edges, node_ids)
        lines = ["flowchart TD"]
        for node in request.nodes:
            lines.append(f"    {self._id(node.node_id)}[{self._label(node.label)}]")
        for edge in request.edges:
            if edge.from_node_id not in node_ids or edge.to_node_id not in node_ids:
                continue
            label = f"|{self._label(edge.label)}|" if edge.label else ""
            lines.append(f"    {self._id(edge.from_node_id)} -->{label} {self._id(edge.to_node_id)}")
        return WorkflowDiagramResult(title=request.title, mermaid="\n".join(lines), warnings=warnings)

    def _validate_edges(self, edges: list[WorkflowEdge], node_ids: set[str]) -> list[str]:
        warnings: list[str] = []
        for edge in edges:
            if edge.from_node_id not in node_ids:
                warnings.append(f"Skipping edge from unknown node: {edge.from_node_id}")
            if edge.to_node_id not in node_ids:
                warnings.append(f"Skipping edge to unknown node: {edge.to_node_id}")
        return warnings

    def _id(self, value: str) -> str:
        cleaned = "".join(character if character.isalnum() else "_" for character in value)
        if not cleaned or cleaned[0].isdigit():
            cleaned = f"node_{cleaned}"
        return cleaned

    def _label(self, value: str | None) -> str:
        if not value:
            return ""
        return value.replace("[", "(").replace("]", ")").replace("\n", " ").strip()
