from app.prompts import PromptRegistry, PromptRenderer


def test_prompt_registry_lists_known_templates():
    registry = PromptRegistry()

    names = {definition.name for definition in registry.list()}

    assert names == {"document_generation", "workflow_diagram"}


def test_prompt_renderer_fills_document_template():
    renderer = PromptRenderer()

    rendered = renderer.render(
        "document_generation",
        {
            "client_name": "DemoClient",
            "objective": "Summarize onboarding steps.",
            "source_text": "The requester submits a sample form.",
            "context": ["Use short sections.", "List assumptions clearly."],
        },
    )

    assert "DemoClient" in rendered
    assert "Use short sections." in rendered
    assert "$client_name" not in rendered
