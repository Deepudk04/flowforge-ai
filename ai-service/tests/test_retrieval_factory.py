from app.config import Settings
from app.retrieval import InMemoryRetriever, create_retriever


def test_retriever_factory_returns_configured_memory_retriever():
    retriever = create_retriever(Settings(retrieval_backend="memory"))

    assert isinstance(retriever, InMemoryRetriever)
    assert retriever.name == "memory"
