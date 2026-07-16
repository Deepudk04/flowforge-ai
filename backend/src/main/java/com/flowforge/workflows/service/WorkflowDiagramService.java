package com.flowforge.workflows.service;

import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramRequest;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramResponse;
import com.flowforge.integration.AiServiceClient;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WorkflowDiagramService {
    private final AiServiceClient aiServiceClient;

    public WorkflowDiagramService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    public WorkflowDiagramResponse generate(WorkflowDiagramRequest request) {
        var result = aiServiceClient.generateWorkflowDiagram(new AiServiceClient.WorkflowDiagramCommand(
                request.title(),
                request.steps().stream()
                        .map(step -> new AiServiceClient.WorkflowStep(step.id(), step.label(), step.nextStepId()))
                        .toList()
        ));

        return new WorkflowDiagramResponse(
                "workflow_" + UUID.randomUUID(),
                request.title(),
                result.mermaid(),
                result.warnings()
        );
    }
}
