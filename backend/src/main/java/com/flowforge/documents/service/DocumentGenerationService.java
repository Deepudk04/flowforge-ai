package com.flowforge.documents.service;

import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationResponse;
import com.flowforge.integration.AiServiceClient;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.GeneratedDocument;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.GeneratedDocumentRepository;
import com.flowforge.persistence.service.GenerationPersistenceService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentGenerationService {
    private final AiServiceClient aiServiceClient;
    private final GeneratedDocumentRepository documentRepository;
    private final DocumentGenerationJobRepository jobRepository;
    private final GenerationPersistenceService persistenceService;

    public DocumentGenerationService(
            AiServiceClient aiServiceClient,
            GeneratedDocumentRepository documentRepository,
            DocumentGenerationJobRepository jobRepository,
            GenerationPersistenceService persistenceService
    ) {
        this.aiServiceClient = aiServiceClient;
        this.documentRepository = documentRepository;
        this.jobRepository = jobRepository;
        this.persistenceService = persistenceService;
    }

    public DocumentGenerationResponse generate(DocumentGenerationRequest request) {
        var documentId = "doc_" + UUID.randomUUID();
        var jobId = "job_" + UUID.randomUUID();
        persistenceService.createJob(jobId, "document", Instant.now());
        try {
            persistenceService.markRunning(jobId, Instant.now());
            var result = aiServiceClient.generateDocument(new AiServiceClient.DocumentGenerationCommand(
                    request.title(),
                    request.documentType(),
                    request.inputContext(),
                    request.tags()
            ));
            var completed = persistenceService.completeDocument(jobId, new GeneratedDocument(
                    documentId,
                    request.title(),
                    request.documentType(),
                    result.content(),
                    Instant.now()
            ), Instant.now());
            return toResponse(completed.document(), completed.job());
        } catch (RuntimeException exception) {
            persistFailureWithoutMasking(jobId, exception);
            throw exception;
        }
    }

    public DocumentGenerationResponse getDocument(String documentId) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        var job = findJob("document", documentId);
        return toResponse(document, job);
    }

    private DocumentGenerationJob findJob(String resourceType, String resourceId) {
        return jobRepository.findFirstByResourceTypeAndResourceIdOrderByCreatedAtDesc(resourceType, resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Generation job not found for resource: " + resourceId));
    }

    private DocumentGenerationResponse toResponse(GeneratedDocument document, DocumentGenerationJob job) {
        return new DocumentGenerationResponse(
                document.getId(),
                document.getTitle(),
                document.getDocumentType(),
                document.getContent(),
                job.getStatus(),
                document.getCreatedAt(),
                job.getId()
        );
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Generation failed";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private void persistFailureWithoutMasking(String jobId, RuntimeException originalException) {
        try {
            persistenceService.markFailed(jobId, truncate(originalException.getMessage()), Instant.now());
        } catch (RuntimeException persistenceException) {
            originalException.addSuppressed(persistenceException);
        }
    }
}
