package com.flowforge.documents.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flowforge.documents.service.DocumentGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentGenerationController.class)
@Import(DocumentGenerationService.class)
@TestPropertySource(properties = "flowforge.security.enabled=false")
class DocumentGenerationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void generateReturnsDocumentResponse() throws Exception {
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
                .andExpect(jsonPath("$.data.title").value("SampleDocument"));
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