from app.config import Settings, get_settings
from app.models import DocumentGenerationRequest, GenerationResult, WorkflowGenerationRequest
from app.providers import AIProvider, MockAIProvider, ProviderRequest


class AIOrchestrator:
    def __init__(self, settings: Settings | None = None, provider: AIProvider | None = None):
        self.settings = settings or get_settings()
        self.provider = provider or MockAIProvider(self.settings)

    def generate_document(self, request: DocumentGenerationRequest) -> GenerationResult:
        context = [item.content for item in request.retrieval_context]
        provider_response = self.provider.generate(
            ProviderRequest(
                instruction="Generate a technical document draft from the provided input.",
                user_content=f"Client: {request.client_name}\nObjective: {request.objective}\nInput: {request.source_text}",
                context=context,
            )
        )
        return GenerationResult(
            kind="document",
            content=provider_response.content,
            model=provider_response.usage.model,
            usage=provider_response.usage,
            retrieval_context_used=[item.source_id for item in request.retrieval_context],
        )

    def generate_workflow(self, request: WorkflowGenerationRequest) -> GenerationResult:
        context = [item.content for item in request.retrieval_context]
        numbered_steps = "\n".join(f"{index + 1}. {step}" for index, step in enumerate(request.steps))
        provider_response = self.provider.generate(
            ProviderRequest(
                instruction="Generate a workflow summary from the provided process steps.",
                user_content=f"Workflow: {request.title}\nSteps:\n{numbered_steps}",
                context=context,
            )
        )
        return GenerationResult(
            kind="workflow",
            content=provider_response.content,
            model=provider_response.usage.model,
            usage=provider_response.usage,
            retrieval_context_used=[item.source_id for item in request.retrieval_context],
        )
