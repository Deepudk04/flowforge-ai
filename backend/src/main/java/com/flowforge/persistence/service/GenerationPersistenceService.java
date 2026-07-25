package com.flowforge.persistence.service;

import com.flowforge.jobs.model.GenerationJobStatus;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.GeneratedDocument;
import com.flowforge.persistence.entity.WorkflowDiagram;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.GeneratedDocumentRepository;
import com.flowforge.persistence.repository.WorkflowDiagramRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenerationPersistenceService {
    private final DocumentGenerationJobRepository jobRepository;
    private final GeneratedDocumentRepository documentRepository;
    private final WorkflowDiagramRepository workflowRepository;

    public GenerationPersistenceService(
            DocumentGenerationJobRepository jobRepository,
            GeneratedDocumentRepository documentRepository,
            WorkflowDiagramRepository workflowRepository
    ) {
        this.jobRepository = jobRepository;
        this.documentRepository = documentRepository;
        this.workflowRepository = workflowRepository;
    }

    @Transactional
    public DocumentGenerationJob createJob(String jobId, String resourceType, Instant createdAt) {
        return jobRepository.save(new DocumentGenerationJob(
                jobId,
                GenerationJobStatus.PENDING,
                resourceType,
                null,
                createdAt
        ));
    }

    @Transactional
    public DocumentGenerationJob markRunning(String jobId, Instant updatedAt) {
        var job = requireJob(jobId);
        job.markRunning(updatedAt);
        return jobRepository.save(job);
    }

    @Transactional
    public CompletedDocument completeDocument(
            String jobId,
            GeneratedDocument generatedDocument,
            Instant updatedAt
    ) {
        var document = documentRepository.save(generatedDocument);
        var job = requireJob(jobId);
        job.markCompleted(document.getId(), updatedAt);
        return new CompletedDocument(document, jobRepository.save(job));
    }

    @Transactional
    public CompletedWorkflow completeWorkflow(
            String jobId,
            WorkflowDiagram workflowDiagram,
            Instant updatedAt
    ) {
        var workflow = workflowRepository.save(workflowDiagram);
        var job = requireJob(jobId);
        job.markCompleted(workflow.getId(), updatedAt);
        return new CompletedWorkflow(workflow, jobRepository.save(job));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String jobId, String failureMessage, Instant updatedAt) {
        var job = requireJob(jobId);
        job.markFailed(failureMessage, updatedAt);
        jobRepository.save(job);
    }

    private DocumentGenerationJob requireJob(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Generation job not found: " + jobId));
    }

    public record CompletedDocument(GeneratedDocument document, DocumentGenerationJob job) {
    }

    public record CompletedWorkflow(WorkflowDiagram workflow, DocumentGenerationJob job) {
    }
}
