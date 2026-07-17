import pytest

from app.config import Settings
from app.core.errors import ProviderConfigurationError
from app.providers import MockAIProvider, ProviderRequest


def test_mock_provider_returns_usage_trace():
    provider = MockAIProvider(Settings())

    response = provider.generate(
        ProviderRequest(
            instruction="Draft a concise process note.",
            user_content="Requester submits a synthetic intake form.",
            context=["Use clear sections."],
        )
    )

    assert response.usage.provider == "mock"
    assert response.usage.model == "flowforge-local-mock"
    assert response.usage.input_tokens > 0
    assert response.usage.output_tokens > 0
    assert "## Procedure" in response.content
    assert "## Review Checklist" in response.content
    assert "1 context item(s) used." in response.content


def test_remote_provider_requires_api_key():
    settings = Settings(llm_provider="openai", llm_api_key="")

    with pytest.raises(ProviderConfigurationError, match="LLM_API_KEY"):
        MockAIProvider(settings)
