class AIServiceError(Exception):
    """Base exception for recoverable AI service failures."""


class ProviderConfigurationError(AIServiceError):
    """Raised when a provider is missing required configuration."""


class ProviderExecutionError(AIServiceError):
    """Raised when a provider cannot complete a generation request."""
