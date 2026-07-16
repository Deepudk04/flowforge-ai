package com.flowforge.documents.service;

import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationResponse;
import com.flowforge.integration.AiServiceClient;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentGenerationService {
    private final AiServiceClient aiServiceClient;

    public DocumentGenerationService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    public DocumentGenerationResponse generate(DocumentGenerationRequest request) {
        var documentId = "doc_" + UUID.randomUUID();
        var result = aiServiceClient.generateDocument(new AiServiceClient.DocumentGenerationCommand(
                request.title(),
                request.documentType(),
                request.inputContext(),
                request.tags() == null ? List.of() : request.tags()
        ));
        return new DocumentGenerationResponse(
                documentId,
                request.title(),
                request.documentType(),
                result.content(),
                "COMPLETED",
                Instant.now()
        );
    }
}
