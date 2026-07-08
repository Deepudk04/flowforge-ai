package com.flowforge.documents.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentGenerationServiceTest {
    @Test
    void generateReturnsCompletedDocument() {
        var service = new DocumentGenerationService();
        var request = new DocumentGenerationRequest("SampleDocument", "document", "DemoClient provided input.", List.of("sample"));

        var response = service.generate(request);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.content()).contains("SampleDocument", "DemoClient provided input.");
        assertThat(response.documentId()).startsWith("doc_");
    }
}