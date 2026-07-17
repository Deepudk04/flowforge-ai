package com.flowforge.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class LocalAiServiceClientTest {
    @Test
    void generateDocumentReturnsMockContent() {
        var client = new LocalAiServiceClient();
        var command = new AiServiceClient.DocumentGenerationCommand(
                "SampleDocument",
                "document",
                "DemoClient provided input.",
                List.of("sample")
        );

        var result = client.generateDocument(command);

        assertThat(result.model()).isEqualTo("local-mock");
        assertThat(result.content()).contains("SampleDocument", "DemoClient provided input.");
        assertThat(result.content()).contains("## Procedure", "## Risks", "## Review Checklist");
    }

    @Test
    void generateWorkflowDiagramSkipsUnknownNextStep() {
        var client = new LocalAiServiceClient();
        var command = new AiServiceClient.WorkflowDiagramCommand(
                "SampleWorkflow",
                List.of(new AiServiceClient.WorkflowStep("start", "Start", "missing"))
        );

        var result = client.generateWorkflowDiagram(command);

        assertThat(result.mermaid()).contains("flowchart TD", "start[Start]");
        assertThat(result.warnings()).containsExactly("Skipping edge to unknown step: missing");
    }
}
