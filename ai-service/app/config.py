from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    service_name: str = "FlowForge AI Service"
    environment: str = "local"
    llm_provider: str = "mock"
    default_model: str = "flowforge-local-mock"
    model_timeout_seconds: int = 30

    model_config = SettingsConfigDict(env_file=".env", extra="ignore", protected_namespaces=())


@lru_cache
def get_settings() -> Settings:
    return Settings()
