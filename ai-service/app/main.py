from fastapi import FastAPI

from app.config import get_settings
from app.core.logging import configure_logging
from app.generation_routes import router as generation_router
from app.health import router as health_router


def create_app() -> FastAPI:
    settings = get_settings()
    configure_logging(settings.log_level)
    app = FastAPI(title=settings.service_name, version="0.1.0")
    app.include_router(health_router)
    app.include_router(generation_router)
    return app


app = create_app()
