from string import Template
from typing import Any

from app.prompts.registry import PromptDefinition, PromptRegistry


class PromptRenderer:
    def __init__(self, registry: PromptRegistry | None = None):
        self.registry = registry or PromptRegistry()

    def render(self, name: str, variables: dict[str, Any]) -> str:
        definition = self.registry.get(name)
        return self.render_definition(definition, variables)

    def render_definition(self, definition: PromptDefinition, variables: dict[str, Any]) -> str:
        template = Template(definition.template_path.read_text(encoding="utf-8"))
        normalized = {key: self._stringify(value) for key, value in variables.items()}
        return template.safe_substitute(normalized).strip()

    def _stringify(self, value: Any) -> str:
        if isinstance(value, list):
            return "\n".join(str(item) for item in value)
        return str(value)
