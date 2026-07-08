package com.flowforge.documents.dto;

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
            List<@Size(max = 40) String> tags
    ) {
    }

    public record DocumentGenerationResponse(
            String documentId,
            String title,
            String documentType,
            String content,
            String status,
            Instant createdAt
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