package com.flowforge.documents.service;

import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentGenerationService {
    public DocumentGenerationResponse generate(DocumentGenerationRequest request) {
        var documentId = "doc_" + UUID.randomUUID();
        var content = "# " + request.title() + "\n\n" + request.inputContext();
        return new DocumentGenerationResponse(
                documentId,
                request.title(),
                request.documentType(),
                content,
                "COMPLETED",
                Instant.now()
        );
    }
}