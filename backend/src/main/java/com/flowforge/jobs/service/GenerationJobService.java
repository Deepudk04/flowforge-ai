package com.flowforge.jobs.service;

import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.jobs.dto.GenerationJobDtos.GenerationJobResponse;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import org.springframework.stereotype.Service;

@Service
public class GenerationJobService {
    private final DocumentGenerationJobRepository jobRepository;

    public GenerationJobService(DocumentGenerationJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public GenerationJobResponse getJob(String jobId) {
        var job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Generation job not found: " + jobId));
        return toResponse(job);
    }

    private GenerationJobResponse toResponse(DocumentGenerationJob job) {
        return new GenerationJobResponse(
                job.getId(),
                job.getStatus(),
                job.getResourceType(),
                job.getResourceId(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
