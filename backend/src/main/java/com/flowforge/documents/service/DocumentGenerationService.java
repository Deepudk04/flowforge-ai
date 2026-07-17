package com.flowforge.documents.service;

import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationResponse;
import com.flowforge.integration.AiServiceClient;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.GeneratedDocument;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.GeneratedDocumentRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentGenerationService {
    private final AiServiceClient aiServiceClient;
    private final GeneratedDocumentRepository documentRepository;
    private final DocumentGenerationJobRepository jobRepository;

    public DocumentGenerationService(
            AiServiceClient aiServiceClient,
            GeneratedDocumentRepository documentRepository,
            DocumentGenerationJobRepository jobRepository
    ) {
        this.aiServiceClient = aiServiceClient;
        this.documentRepository = documentRepository;
        this.jobRepository = jobRepository;
    }

    public DocumentGenerationResponse generate(DocumentGenerationRequest request) {
        var documentId = "doc_" + UUID.randomUUID();
        var job = createJob("document");
        try {
            job.markRunning(Instant.now());
            jobRepository.save(job);
            var result = aiServiceClient.generateDocument(new AiServiceClient.DocumentGenerationCommand(
                    request.title(),
                    request.documentType(),
                    request.inputContext(),
                    request.tags() == null ? List.of() : request.tags()
            ));
            var document = documentRepository.save(new GeneratedDocument(
                    documentId,
                    request.title(),
                    request.documentType(),
                    result.content(),
                    Instant.now()
            ));
            job.markCompleted(document.getId(), Instant.now());
            jobRepository.save(job);
            return toResponse(document, job);
        } catch (RuntimeException exception) {
            job.markFailed(truncate(exception.getMessage()), Instant.now());
            jobRepository.save(job);
            throw exception;
        }
    }

    public DocumentGenerationResponse getDocument(String documentId) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        var job = findJob("document", documentId);
        return toResponse(document, job);
    }

    private DocumentGenerationJob createJob(String resourceType) {
        return jobRepository.save(new DocumentGenerationJob(
                "job_" + UUID.randomUUID(),
                "PENDING",
                resourceType,
                null,
                Instant.now()
        ));
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
}
