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
        context_note = f" Context items: {len(request.context)}." if request.context else ""
        content = f"{request.instruction}\n\n{request.user_content.strip()}{context_note}"
        return ProviderResponse(
            content=content,
            usage=UsageTrace(
                provider=self.name,
                model=self.model,
                input_tokens=len((request.instruction + " " + request.user_content).split()),
                output_tokens=len(content.split()),
            ),
        )
