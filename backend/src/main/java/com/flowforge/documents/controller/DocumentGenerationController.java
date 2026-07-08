package com.flowforge.documents.controller;

import com.flowforge.common.ApiResponse;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationRequest;
import com.flowforge.documents.dto.DocumentDtos.DocumentGenerationResponse;
import com.flowforge.documents.service.DocumentGenerationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentGenerationController {
    private final DocumentGenerationService service;

    public DocumentGenerationController(DocumentGenerationService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ApiResponse<DocumentGenerationResponse> generate(@Valid @RequestBody DocumentGenerationRequest request) {
        return ApiResponse.ok(service.generate(request));
    }
}