package com.flowforge.workflows.service;

import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramRequest;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WorkflowDiagramService {
    public WorkflowDiagramResponse generate(WorkflowDiagramRequest request) {
        var nodeIds = new HashSet<String>();
        var lines = new ArrayList<String>();
        var warnings = new ArrayList<String>();
        lines.add("flowchart TD");

        for (var step : request.steps()) {
            nodeIds.add(step.id());
            lines.add("    " + normalizeId(step.id()) + "[" + sanitizeLabel(step.label()) + "]");
        }

        for (var step : request.steps()) {
            if (step.nextStepId() == null || step.nextStepId().isBlank()) {
                continue;
            }
            if (!nodeIds.contains(step.nextStepId())) {
                warnings.add("Skipping edge to unknown step: " + step.nextStepId());
                continue;
            }
            lines.add("    " + normalizeId(step.id()) + " --> " + normalizeId(step.nextStepId()));
        }

        return new WorkflowDiagramResponse(
                "workflow_" + UUID.randomUUID(),
                request.title(),
                String.join("\n", lines),
                List.copyOf(warnings)
        );
    }

    private String normalizeId(String value) {
        var cleaned = value.replaceAll("[^A-Za-z0-9_]", "_");
        return cleaned.matches("^[0-9].*") ? "step_" + cleaned : cleaned;
    }

    private String sanitizeLabel(String value) {
        return value.replace('[', '(').replace(']', ')').replace('\n', ' ').trim();
    }
}