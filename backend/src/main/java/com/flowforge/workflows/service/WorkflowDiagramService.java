package com.flowforge.workflows.service;

import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramRequest;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramResponse;
import com.flowforge.integration.AiServiceClient;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.WorkflowDiagram;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.WorkflowDiagramRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WorkflowDiagramService {
    private final AiServiceClient aiServiceClient;
    private final WorkflowDiagramRepository workflowRepository;
    private final DocumentGenerationJobRepository jobRepository;

    public WorkflowDiagramService(
            AiServiceClient aiServiceClient,
            WorkflowDiagramRepository workflowRepository,
            DocumentGenerationJobRepository jobRepository
    ) {
        this.aiServiceClient = aiServiceClient;
        this.workflowRepository = workflowRepository;
        this.jobRepository = jobRepository;
    }

    public WorkflowDiagramResponse generate(WorkflowDiagramRequest request) {
        var workflowId = "workflow_" + UUID.randomUUID();
        var job = createJob("workflow");
        try {
            job.markRunning(Instant.now());
            jobRepository.save(job);
            var result = aiServiceClient.generateWorkflowDiagram(new AiServiceClient.WorkflowDiagramCommand(
                    request.title(),
                    request.steps().stream()
                            .map(step -> new AiServiceClient.WorkflowStep(step.id(), step.label(), step.nextStepId()))
                            .toList()
            ));
            var workflow = workflowRepository.save(new WorkflowDiagram(
                    workflowId,
                    request.title(),
                    result.mermaid(),
                    serializeWarnings(result.warnings()),
                    Instant.now()
            ));
            job.markCompleted(workflow.getId(), Instant.now());
            jobRepository.save(job);
            return toResponse(workflow, job);
        } catch (RuntimeException exception) {
            job.markFailed(truncate(exception.getMessage()), Instant.now());
            jobRepository.save(job);
            throw exception;
        }
    }

    public WorkflowDiagramResponse getWorkflow(String workflowId) {
        var workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow diagram not found: " + workflowId));
        var job = findJob("workflow", workflowId);
        return toResponse(workflow, job);
    }

    private DocumentGenerationJob createJob(String resourceType) {
        return jobRepository.save(new DocumentGenerationJob(
                "job_" + UUID.randomUUID(),
                "PENDING",
                resourceType,
                null,
                Instant.now()
        ));
    }

    private DocumentGenerationJob findJob(String resourceType, String resourceId) {
        return jobRepository.findFirstByResourceTypeAndResourceIdOrderByCreatedAtDesc(resourceType, resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Generation job not found for resource: " + resourceId));
    }

    private WorkflowDiagramResponse toResponse(WorkflowDiagram workflow, DocumentGenerationJob job) {
        return new WorkflowDiagramResponse(
                workflow.getId(),
                workflow.getTitle(),
                workflow.getMermaid(),
                deserializeWarnings(workflow.getWarnings()),
                job.getStatus(),
                workflow.getCreatedAt(),
                job.getId()
        );
    }

    private String serializeWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return null;
        }
        return String.join("\n", warnings);
    }

    private List<String> deserializeWarnings(String warnings) {
        if (warnings == null || warnings.isBlank()) {
            return List.of();
        }
        return Arrays.stream(warnings.split("\\R"))
                .filter(line -> !line.isBlank())
                .toList();
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Generation failed";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
