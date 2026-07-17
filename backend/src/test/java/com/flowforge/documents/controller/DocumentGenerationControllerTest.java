package com.flowforge.documents.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationResponse;
import com.flowforge.documents.service.DocumentGenerationService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentGenerationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "flowforge.security.enabled=false"
})
class DocumentGenerationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentGenerationService service;

    @Test
    void generateReturnsDocumentResponse() throws Exception {
        when(service.generate(any())).thenReturn(new DocumentGenerationResponse(
                "doc_123",
                "SampleDocument",
                "document",
                "# SampleDocument",
                "COMPLETED",
                Instant.parse("2026-07-16T00:00:00Z"),
                "job_123"
        ));

        mockMvc.perform(post("/api/v1/documents/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "SampleDocument",
                                  "documentType": "document",
                                  "inputContext": "DemoClient provided input.",
                                  "tags": ["sample"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("SampleDocument"))
                .andExpect(jsonPath("$.data.jobId").value("job_123"));
    }

    @Test
    void getDocumentReturnsPersistedDocument() throws Exception {
        when(service.getDocument("doc_123")).thenReturn(new DocumentGenerationResponse(
                "doc_123",
                "SampleDocument",
                "document",
                "# SampleDocument",
                "COMPLETED",
                Instant.parse("2026-07-16T00:00:00Z"),
                "job_123"
        ));

        mockMvc.perform(get("/api/v1/documents/doc_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentId").value("doc_123"))
                .andExpect(jsonPath("$.data.jobId").value("job_123"));
    }

    @Test
    void generateValidatesRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/documents/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }
}
