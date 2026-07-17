package com.flowforge.workflows.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.flowforge.integration.AiServiceClient;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.WorkflowDiagram;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.WorkflowDiagramRepository;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramRequest;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowStepRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WorkflowDiagramServiceTest {
    @Test
    void generatePersistsMermaidDiagramAndJob() {
        var aiClient = mock(AiServiceClient.class);
        var workflowRepository = mock(WorkflowDiagramRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        when(aiClient.generateWorkflowDiagram(any())).thenReturn(new AiServiceClient.WorkflowDiagramResult(
                "flowchart TD\n    intake[Receive intake]\n    intake --> review",
                List.of()
        ));
        when(workflowRepository.save(any(WorkflowDiagram.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.save(any(DocumentGenerationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new WorkflowDiagramService(aiClient, workflowRepository, jobRepository);
        var request = new WorkflowDiagramRequest("SampleWorkflow", List.of(
                new WorkflowStepRequest("intake", "Receive intake", "review"),
                new WorkflowStepRequest("review", "Review request", null)
        ));

        var response = service.generate(request);

        assertThat(response.mermaid()).contains("flowchart TD", "intake[Receive intake]", "intake --> review");
        assertThat(response.warnings()).isEmpty();
        assertThat(response.workflowId()).startsWith("workflow_");
        assertThat(response.jobId()).startsWith("job_");
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void getWorkflowReadsPersistedDiagramAndJob() {
        var aiClient = mock(AiServiceClient.class);
        var workflowRepository = mock(WorkflowDiagramRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        var workflow = new WorkflowDiagram(
                "workflow_123",
                "SampleWorkflow",
                "flowchart TD",
                "Skipping edge to unknown node: missing",
                Instant.now()
        );
        var job = new DocumentGenerationJob("job_123", "PENDING", "workflow", null, Instant.now());
        job.markCompleted("workflow_123", Instant.now());
        when(workflowRepository.findById("workflow_123")).thenReturn(Optional.of(workflow));
        when(jobRepository.findFirstByResourceTypeAndResourceIdOrderByCreatedAtDesc("workflow", "workflow_123"))
                .thenReturn(Optional.of(job));
        var service = new WorkflowDiagramService(aiClient, workflowRepository, jobRepository);

        var response = service.getWorkflow("workflow_123");

        assertThat(response.workflowId()).isEqualTo("workflow_123");
        assertThat(response.jobId()).isEqualTo("job_123");
        assertThat(response.warnings()).containsExactly("Skipping edge to unknown node: missing");
    }
}
