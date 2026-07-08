package com.flowforge.jobs.dto;

import java.time.Instant;

public final class GenerationJobDtos {
    private GenerationJobDtos() {
    }

    public record GenerationJobResponse(
            String jobId,
            String status,
            String resourceType,
            String resourceId,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}