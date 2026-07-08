package com.flowforge.workflows.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramRequest;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowStepRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkflowDiagramServiceTest {
    @Test
    void generateReturnsMermaidDiagram() {
        var service = new WorkflowDiagramService();
        var request = new WorkflowDiagramRequest("SampleWorkflow", List.of(
                new WorkflowStepRequest("intake", "Receive intake", "review"),
                new WorkflowStepRequest("review", "Review request", null)
        ));

        var response = service.generate(request);

        assertThat(response.mermaid()).contains("flowchart TD", "intake[Receive intake]", "intake --> review");
        assertThat(response.warnings()).isEmpty();
    }
}