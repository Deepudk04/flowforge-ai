package com.flowforge.documents.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import com.flowforge.integration.AiServiceClient;
import com.flowforge.jobs.model.GenerationJobStatus;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.GeneratedDocument;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.GeneratedDocumentRepository;
import com.flowforge.persistence.service.GenerationPersistenceService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DocumentGenerationServiceTest {
    @Test
    void generatePersistsCompletedDocumentAndJob() {
        var aiClient = mock(AiServiceClient.class);
        var documentRepository = mock(GeneratedDocumentRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        var persistenceService = mock(GenerationPersistenceService.class);
        when(aiClient.generateDocument(any())).thenReturn(new AiServiceClient.DocumentGenerationResult(
                "# SampleDocument\n\nGenerated content",
                "test-model",
                List.of()
        ));
        when(persistenceService.completeDocument(anyString(), any(GeneratedDocument.class), any(Instant.class)))
                .thenAnswer(invocation -> {
                    String jobId = invocation.getArgument(0);
                    GeneratedDocument document = invocation.getArgument(1);
                    var job = runningJob(jobId, "document");
                    job.markCompleted(document.getId(), Instant.now());
                    return new GenerationPersistenceService.CompletedDocument(document, job);
                });
        var service = new DocumentGenerationService(aiClient, documentRepository, jobRepository, persistenceService);
        var request = new DocumentGenerationRequest("SampleDocument", "document", "DemoClient provided input.", List.of("sample"));

        var response = service.generate(request);

        assertThat(response.status()).isEqualTo(GenerationJobStatus.COMPLETED);
        assertThat(response.content()).contains("Generated content");
        assertThat(response.documentId()).startsWith("doc_");
        assertThat(response.jobId()).startsWith("job_");
    }

    @Test
    void getDocumentReadsPersistedDocumentAndJob() {
        var aiClient = mock(AiServiceClient.class);
        var documentRepository = mock(GeneratedDocumentRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        var persistenceService = mock(GenerationPersistenceService.class);
        var document = new GeneratedDocument("doc_123", "SampleDocument", "document", "content", java.time.Instant.now());
        var job = new DocumentGenerationJob(
                "job_123",
                GenerationJobStatus.PENDING,
                "document",
                null,
                java.time.Instant.now()
        );
        job.markRunning(java.time.Instant.now());
        job.markCompleted("doc_123", java.time.Instant.now());
        when(documentRepository.findById("doc_123")).thenReturn(Optional.of(document));
        when(jobRepository.findFirstByResourceTypeAndResourceIdOrderByCreatedAtDesc("document", "doc_123"))
                .thenReturn(Optional.of(job));
        var service = new DocumentGenerationService(aiClient, documentRepository, jobRepository, persistenceService);

        var response = service.getDocument("doc_123");

        assertThat(response.documentId()).isEqualTo("doc_123");
        assertThat(response.jobId()).isEqualTo("job_123");
        assertThat(response.status()).isEqualTo(GenerationJobStatus.COMPLETED);
    }

    @Test
    void generateMarksJobFailedWhenAiClientFails() {
        var aiClient = mock(AiServiceClient.class);
        var documentRepository = mock(GeneratedDocumentRepository.class);
        var jobRepository = mock(DocumentGenerationJobRepository.class);
        var persistenceService = mock(GenerationPersistenceService.class);
        when(aiClient.generateDocument(any())).thenThrow(new IllegalStateException("AI unavailable"));
        var service = new DocumentGenerationService(aiClient, documentRepository, jobRepository, persistenceService);
        var request = new DocumentGenerationRequest("SampleDocument", "document", "DemoClient provided input.", List.of());

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AI unavailable");

        verify(persistenceService).createJob(anyString(), org.mockito.ArgumentMatchers.eq("document"), any(Instant.class));
        verify(persistenceService).markRunning(anyString(), any(Instant.class));
        verify(persistenceService).markFailed(anyString(), org.mockito.ArgumentMatchers.eq("AI unavailable"), any(Instant.class));
    }

    @Test
    void failurePersistenceDoesNotMaskAiFailure() {
        var aiClient = mock(AiServiceClient.class);
        var persistenceService = mock(GenerationPersistenceService.class);
        when(aiClient.generateDocument(any())).thenThrow(new IllegalStateException("AI unavailable"));
        org.mockito.Mockito.doThrow(new IllegalStateException("Database unavailable"))
                .when(persistenceService).markFailed(anyString(), anyString(), any(Instant.class));
        var service = new DocumentGenerationService(
                aiClient,
                mock(GeneratedDocumentRepository.class),
                mock(DocumentGenerationJobRepository.class),
                persistenceService
        );

        assertThatThrownBy(() -> service.generate(
                new DocumentGenerationRequest("SampleDocument", "document", "Synthetic input.", null)
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI unavailable")
                .satisfies(exception -> assertThat(exception.getSuppressed())
                        .extracting(Throwable::getMessage)
                        .containsExactly("Database unavailable"));
    }

    private static DocumentGenerationJob runningJob(String jobId, String resourceType) {
        var job = new DocumentGenerationJob(
                jobId,
                GenerationJobStatus.PENDING,
                resourceType,
                null,
                Instant.now()
        );
        job.markRunning(Instant.now());
        return job;
    }
}
