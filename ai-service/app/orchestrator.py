from app.config import Settings, get_settings
from app.models import DocumentGenerationRequest, GenerationResult, WorkflowGenerationRequest
from app.prompts import PromptRenderer
from app.providers import AIProvider, MockAIProvider, ProviderRequest


class AIOrchestrator:
    def __init__(
        self,
        settings: Settings | None = None,
        provider: AIProvider | None = None,
        prompt_renderer: PromptRenderer | None = None,
    ):
        self.settings = settings or get_settings()
        self.provider = provider or MockAIProvider(self.settings)
        self.prompt_renderer = prompt_renderer or PromptRenderer()

    def generate_document(self, request: DocumentGenerationRequest) -> GenerationResult:
        context = [item.content for item in request.retrieval_context]
        prompt = self.prompt_renderer.render(
            "document_generation",
            {
                "client_name": request.client_name,
                "objective": request.objective,
                "source_text": request.source_text,
                "context": context or ["No additional context provided."],
            },
        )
        provider_response = self.provider.generate(
            ProviderRequest(
                instruction="Draft a document using the rendered prompt.",
                user_content=prompt,
                context=context,
                artifact_title=request.document_id,
                artifact_scope=request.source_text,
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
        steps = [f"{index + 1}. {step}" for index, step in enumerate(request.steps)]
        prompt = self.prompt_renderer.render(
            "workflow_diagram",
            {
                "workflow_title": request.title,
                "steps": steps,
            },
        )
        provider_response = self.provider.generate(
            ProviderRequest(
                instruction="Draft a workflow response using the rendered prompt.",
                user_content=prompt,
                context=context,
                artifact_title=request.title,
                procedure_items=request.steps,
            )
        )
        return GenerationResult(
            kind="workflow",
            content=provider_response.content,
            model=provider_response.usage.model,
            usage=provider_response.usage,
            retrieval_context_used=[item.source_id for item in request.retrieval_context],
        )
