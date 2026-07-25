import pytest
from pydantic import ValidationError

from app.config import Settings
from app.providers import MockAIProvider, ProviderRequest, create_provider


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


def test_unsupported_provider_is_rejected_by_configuration():
    with pytest.raises(ValidationError, match="llm_provider"):
        Settings(llm_provider="openai")


def test_provider_factory_returns_configured_mock():
    provider = create_provider(Settings(default_model="synthetic-model"))

    assert provider.name == "mock"
    assert provider.model == "synthetic-model"
