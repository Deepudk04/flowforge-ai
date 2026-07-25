package com.flowforge.jobs.dto;

import com.flowforge.jobs.model.GenerationJobStatus;
import java.time.Instant;

public final class GenerationJobDtos {
    private GenerationJobDtos() {
    }

    public record GenerationJobResponse(
            String jobId,
            GenerationJobStatus status,
            String resourceType,
            String resourceId,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
