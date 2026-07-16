package com.flowforge.integration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "flowforge.ai-service", name = "mode", havingValue = "local")
public class LocalAiServiceClient implements AiServiceClient {
    @Override
    public DocumentGenerationResult generateDocument(DocumentGenerationCommand command) {
        var content = "# " + command.title() + "\n\n" + command.inputContext();
        return new DocumentGenerationResult(content, "local-mock", List.of());
    }

    @Override
    public WorkflowDiagramResult generateWorkflowDiagram(WorkflowDiagramCommand command) {
        var nodeIds = command.steps().stream().map(WorkflowStep::id).toList();
        var warnings = new ArrayList<String>();
        var lines = new ArrayList<String>();
        lines.add("flowchart TD");
        for (var step : command.steps()) {
            lines.add("    " + normalizeId(step.id()) + "[" + sanitizeLabel(step.label()) + "]");
        }
        for (var step : command.steps()) {
            if (step.nextStepId() == null || step.nextStepId().isBlank()) {
                continue;
            }
            if (!nodeIds.contains(step.nextStepId())) {
                warnings.add("Skipping edge to unknown step: " + step.nextStepId());
                continue;
            }
            lines.add("    " + normalizeId(step.id()) + " --> " + normalizeId(step.nextStepId()));
        }
        return new WorkflowDiagramResult(String.join("\n", lines), List.copyOf(warnings));
    }

    private String normalizeId(String value) {
        var cleaned = value.replaceAll("[^A-Za-z0-9_]", "_");
        return cleaned.matches("^[0-9].*") ? "step_" + cleaned : cleaned;
    }

    private String sanitizeLabel(String value) {
        return value.replace('[', '(').replace(']', ')').replace('\n', ' ').trim();
    }
}
