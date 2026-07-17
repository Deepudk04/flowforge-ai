package com.flowforge.documents.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import com.flowforge.integration.AiServiceClient;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.GeneratedDocument;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.GeneratedDocumentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DocumentGenerationServiceTest {
    @Test
    void generatePersistsCompletedDocumentAndJob() {
        var aiClient = mock(AiServiceClient.class);
        var documentRepository = mock(GeneratedDocumentRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        when(aiClient.generateDocument(any())).thenReturn(new AiServiceClient.DocumentGenerationResult(
                "# SampleDocument\n\nGenerated content",
                "test-model",
                List.of()
        ));
        when(documentRepository.save(any(GeneratedDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.save(any(DocumentGenerationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new DocumentGenerationService(aiClient, documentRepository, jobRepository);
        var request = new DocumentGenerationRequest("SampleDocument", "document", "DemoClient provided input.", List.of("sample"));

        var response = service.generate(request);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.content()).contains("Generated content");
        assertThat(response.documentId()).startsWith("doc_");
        assertThat(response.jobId()).startsWith("job_");
    }

    @Test
    void getDocumentReadsPersistedDocumentAndJob() {
        var aiClient = mock(AiServiceClient.class);
        var documentRepository = mock(GeneratedDocumentRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        var document = new GeneratedDocument("doc_123", "SampleDocument", "document", "content", java.time.Instant.now());
        var job = new DocumentGenerationJob("job_123", "PENDING", "document", null, java.time.Instant.now());
        job.markCompleted("doc_123", java.time.Instant.now());
        when(documentRepository.findById("doc_123")).thenReturn(Optional.of(document));
        when(jobRepository.findFirstByResourceTypeAndResourceIdOrderByCreatedAtDesc("document", "doc_123"))
                .thenReturn(Optional.of(job));
        var service = new DocumentGenerationService(aiClient, documentRepository, jobRepository);

        var response = service.getDocument("doc_123");

        assertThat(response.documentId()).isEqualTo("doc_123");
        assertThat(response.jobId()).isEqualTo("job_123");
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void generateMarksJobFailedWhenAiClientFails() {
        var aiClient = mock(AiServiceClient.class);
        var documentRepository = mock(GeneratedDocumentRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        var savedStatuses = new ArrayList<String>();
        when(aiClient.generateDocument(any())).thenThrow(new IllegalStateException("AI unavailable"));
        when(jobRepository.save(any(DocumentGenerationJob.class))).thenAnswer(invocation -> {
            DocumentGenerationJob job = invocation.getArgument(0);
            savedStatuses.add(job.getStatus());
            return job;
        });
        var service = new DocumentGenerationService(aiClient, documentRepository, jobRepository);
        var request = new DocumentGenerationRequest("SampleDocument", "document", "DemoClient provided input.", List.of());

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AI unavailable");

        assertThat(savedStatuses).containsExactly("PENDING", "RUNNING", "FAILED");
    }
}
