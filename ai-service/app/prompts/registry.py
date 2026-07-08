from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class PromptDefinition:
    name: str
    version: str
    template_path: Path
    description: str


class PromptRegistry:
    def __init__(self, template_dir: Path | None = None):
        self.template_dir = template_dir or Path(__file__).parent / "templates"
        self._definitions = {
            "document_generation": PromptDefinition(
                name="document_generation",
                version="v1",
                template_path=self.template_dir / "document_generation.v1.txt",
                description="Drafts a technical document from structured input and context.",
            ),
            "workflow_diagram": PromptDefinition(
                name="workflow_diagram",
                version="v1",
                template_path=self.template_dir / "workflow_diagram.v1.txt",
                description="Drafts Mermaid diagram text from process steps.",
            ),
        }

    def get(self, name: str) -> PromptDefinition:
        try:
            return self._definitions[name]
        except KeyError as exc:
            raise KeyError(f"Unknown prompt template: {name}") from exc

    def list(self) -> list[PromptDefinition]:
        return list(self._definitions.values())
