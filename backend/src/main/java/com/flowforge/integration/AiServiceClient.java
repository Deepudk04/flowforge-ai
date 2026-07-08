package com.flowforge.integration;

import java.util.List;

public interface AiServiceClient {
    DocumentGenerationResult generateDocument(DocumentGenerationCommand command);

    WorkflowDiagramResult generateWorkflowDiagram(WorkflowDiagramCommand command);

    record DocumentGenerationCommand(String title, String documentType, String inputContext, List<String> tags) {
    }

    record DocumentGenerationResult(String content, String model, List<String> retrievedSourceIds) {
    }

    record WorkflowDiagramCommand(String title, List<WorkflowStep> steps) {
    }

    record WorkflowStep(String id, String label, String nextStepId) {
    }

    record WorkflowDiagramResult(String mermaid, List<String> warnings) {
    }
}