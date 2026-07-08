package com.flowforge.workflows.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class WorkflowDtos {
    private WorkflowDtos() {
    }

    public record WorkflowDiagramRequest(
            @NotBlank @Size(max = 160) String title,
            @NotEmpty List<@Valid WorkflowStepRequest> steps
    ) {
    }

    public record WorkflowStepRequest(
            @NotBlank @Size(max = 80) String id,
            @NotBlank @Size(max = 120) String label,
            @Size(max = 80) String nextStepId
    ) {
    }

    public record WorkflowDiagramResponse(
            String workflowId,
            String title,
            String mermaid,
            List<String> warnings
    ) {
    }
}