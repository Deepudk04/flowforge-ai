package com.flowforge.documents.dto;

import com.flowforge.jobs.model.GenerationJobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class DocumentDtos {
    private DocumentDtos() {
    }

    public record DocumentGenerationRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 80) String documentType,
            @NotBlank @Size(max = 12000) String inputContext,
            @Size(max = 20) List<@NotBlank @Size(max = 40) String> tags
    ) {
        public DocumentGenerationRequest {
            tags = tags == null ? List.of() : tags;
        }
    }

    public record DocumentGenerationResponse(
            String documentId,
            String title,
            String documentType,
            String content,
            GenerationJobStatus status,
            Instant createdAt,
            String jobId
    ) {
    }

    public record RetrievalContextResponse(
            String sourceId,
            String title,
            String excerpt,
            double score
    ) {
    }
}
