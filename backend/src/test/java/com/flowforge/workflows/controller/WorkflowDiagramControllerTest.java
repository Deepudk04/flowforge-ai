package com.flowforge.workflows.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flowforge.workflows.service.WorkflowDiagramService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkflowDiagramController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WorkflowDiagramService.class)
@TestPropertySource(properties = "flowforge.security.enabled=false")
class WorkflowDiagramControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void generateReturnsWorkflowDiagramResponse() throws Exception {
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
                .andExpect(jsonPath("$.data.mermaid").value(org.hamcrest.Matchers.containsString("intake --> approval")));
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
