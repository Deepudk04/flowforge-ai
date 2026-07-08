from functools import lru_cache
from typing import Literal

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

Environment = Literal["local", "test", "production"]
ProviderName = Literal["mock", "openai", "anthropic", "gemini", "azure_openai"]
RetrievalBackend = Literal["memory", "pgvector"]


class Settings(BaseSettings):
    service_name: str = "FlowForge AI Service"
    environment: Environment = "local"
    log_level: str = "INFO"

    llm_provider: ProviderName = "mock"
    llm_api_key: str = ""
    default_model: str = "flowforge-local-mock"
    model_timeout_seconds: int = Field(default=30, ge=1, le=300)

    embedding_provider: ProviderName = "mock"
    embedding_model: str = "flowforge-local-embedding"
    embedding_dimensions: int = Field(default=384, ge=1, le=4096)

    retrieval_backend: RetrievalBackend = "memory"
    retrieval_default_limit: int = Field(default=5, ge=1, le=50)
    vector_db_url: str = ""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore", protected_namespaces=())

    @property
    def remote_llm_configured(self) -> bool:
        return self.llm_provider == "mock" or bool(self.llm_api_key)

    @property
    def vector_store_configured(self) -> bool:
        return self.retrieval_backend == "memory" or bool(self.vector_db_url)


@lru_cache
def get_settings() -> Settings:
    return Settings()
