package com.flowforge.sources.dto;

import com.flowforge.sources.model.SourceStatus;
import com.flowforge.sources.model.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class SourceDtos {
    private SourceDtos() {
    }

    public record RejectSourceRequest(
            @NotBlank @Size(max = 2000) String reason
    ) {
    }

    public record SourceResponse(
            String id,
            String workspaceId,
            String title,
            String originalFilename,
            SourceType sourceType,
            String contentType,
            String contentHash,
            long sizeBytes,
            SourceStatus status,
            String approvedBy,
            Instant approvedAt,
            String rejectionReason,
            String createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
