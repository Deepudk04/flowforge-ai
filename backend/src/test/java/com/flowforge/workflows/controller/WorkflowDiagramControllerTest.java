package com.flowforge.workflows.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramResponse;
import com.flowforge.workflows.service.WorkflowDiagramService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkflowDiagramController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "flowforge.security.enabled=false"
})
class WorkflowDiagramControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowDiagramService service;

    @Test
    void generateReturnsWorkflowDiagramResponse() throws Exception {
        when(service.generate(any())).thenReturn(new WorkflowDiagramResponse(
                "workflow_123",
                "Vendor Approval Workflow",
                "flowchart TD\n    intake --> approval",
                List.of(),
                "COMPLETED",
                Instant.parse("2026-07-16T00:00:00Z"),
                "job_123"
        ));

        mockMvc.perform(post("/api/v1/workflows/diagram")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Vendor Approval Workflow",
                                  "steps": [
                                    {
                                      "id": "intake",
                                      "label": "Submit vendor intake form",
                                      "nextStepId": "approval"
                                    },
                                    {
                                      "id": "approval",
                                      "label": "Approve vendor"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Vendor Approval Workflow"))
                .andExpect(jsonPath("$.data.mermaid").value(org.hamcrest.Matchers.containsString("intake --> approval")))
                .andExpect(jsonPath("$.data.jobId").value("job_123"));
    }

    @Test
    void getWorkflowReturnsPersistedDiagram() throws Exception {
        when(service.getWorkflow("workflow_123")).thenReturn(new WorkflowDiagramResponse(
                "workflow_123",
                "Vendor Approval Workflow",
                "flowchart TD\n    intake --> approval",
                List.of(),
                "COMPLETED",
                Instant.parse("2026-07-16T00:00:00Z"),
                "job_123"
        ));

        mockMvc.perform(get("/api/v1/workflows/workflow_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workflowId").value("workflow_123"))
                .andExpect(jsonPath("$.data.jobId").value("job_123"));
    }

    @Test
    void generateValidatesRequiredSteps() throws Exception {
        mockMvc.perform(post("/api/v1/workflows/diagram")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Vendor Approval Workflow",
                                  "steps": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }
}
