from dataclasses import dataclass
from typing import Protocol

from app.config import Settings
from app.core.errors import ProviderConfigurationError
from app.models import UsageTrace


@dataclass(frozen=True)
class ProviderRequest:
    instruction: str
    user_content: str
    context: list[str]
    artifact_title: str | None = None
    artifact_scope: str | None = None
    procedure_items: list[str] | None = None


@dataclass(frozen=True)
class ProviderResponse:
    content: str
    usage: UsageTrace


class AIProvider(Protocol):
    name: str
    model: str

    def generate(self, request: ProviderRequest) -> ProviderResponse:
        raise NotImplementedError


class MockAIProvider:
    name = "mock"

    def __init__(self, settings: Settings):
        if settings.llm_provider != "mock" and not settings.llm_api_key:
            raise ProviderConfigurationError("Remote LLM provider requires LLM_API_KEY.")
        self.model = settings.default_model

    def generate(self, request: ProviderRequest) -> ProviderResponse:
        content = self._structured_content(request)
        return ProviderResponse(
            content=content,
            usage=UsageTrace(
                provider=self.name,
                model=self.model,
                input_tokens=len((request.instruction + " " + request.user_content).split()),
                output_tokens=len(content.split()),
            ),
        )

    def _structured_content(self, request: ProviderRequest) -> str:
        source_lines = self._meaningful_lines(request.user_content)
        title = request.artifact_title or self._title(source_lines)
        context_note = f"{len(request.context)} context item(s) used." if request.context else "No retrieval context used."
        procedure_items = self._procedure_items(source_lines, request.procedure_items)
        return "\n".join(
            [
                f"# {title}",
                "",
                "## Objective",
                self._first_sentence(source_lines, "Draft a synthetic process artifact from the provided context."),
                "",
                "## Scope",
                request.artifact_scope or self._scope(source_lines),
                "",
                "## Procedure",
                *procedure_items,
                "",
                "## Assumptions",
                "- The example uses synthetic public-safe data only.",
                "- Missing owners, systems, or dates should be confirmed before operational use.",
                f"- {context_note}",
                "",
                "## Risks",
                "- Incomplete source context can leave undocumented handoffs or controls.",
                "- Human review is required before using this draft as a production procedure.",
                "",
                "## Owners",
                "- Process owner: Demo process owner.",
                "- Reviewer: Demo compliance reviewer.",
                "",
                "## Review Checklist",
                "- [ ] Inputs and outputs are clear.",
                "- [ ] Decision points and exceptions are documented.",
                "- [ ] Evidence requirements are reviewable.",
                "- [ ] Generated content cites only synthetic context.",
            ]
        )

    def _meaningful_lines(self, content: str) -> list[str]:
        return [
            line.strip(" -")
            for line in content.splitlines()
            if line.strip() and not line.strip().startswith("```")
        ]

    def _title(self, lines: list[str]) -> str:
        for line in lines:
            if "SampleDocument" in line:
                return "SampleDocument"
            if "SampleWorkflow" in line:
                return "SampleWorkflow"
            if ":" in line and len(line) <= 120:
                return line.rsplit(":", 1)[-1].strip() or "Generated Process Document"
        return "Generated Process Document"

    def _first_sentence(self, lines: list[str], fallback: str) -> str:
        for line in lines:
            if len(line) > 20 and not line.lower().startswith(("use ", "context", "steps")):
                return line.rstrip(".") + "."
        return fallback

    def _scope(self, lines: list[str]) -> str:
        for line in lines:
            if "DemoClient" in line or "synthetic" in line.lower():
                return line.rstrip(".") + "."
        return "This draft covers the process described in the provided synthetic source material."

    def _procedure_items(self, lines: list[str], explicit_items: list[str] | None = None) -> list[str]:
        candidates = explicit_items or [
            line
            for line in lines
            if len(line) > 8 and not line.startswith("#") and not line.lower().startswith(("context", "objective"))
        ][:4]
        if not candidates:
            candidates = [
                "Collect the request and required supporting details",
                "Validate the input for completeness and policy alignment",
                "Complete the process task and record the result",
                "Review exceptions and archive evidence",
            ]
        return [f"{index}. {self._strip_numbering(item).rstrip('.')}" for index, item in enumerate(candidates, start=1)]

    def _strip_numbering(self, value: str) -> str:
        parts = value.split(". ", 1)
        if len(parts) == 2 and parts[0].isdigit():
            return parts[1]
        return value
