package com.flowforge.sources.controller;

import com.flowforge.common.ApiResponse;
import com.flowforge.common.PageResponse;
import com.flowforge.sources.dto.SourceDtos.RejectSourceRequest;
import com.flowforge.sources.dto.SourceDtos.SourceResponse;
import com.flowforge.sources.service.SourceDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SourceDocumentController {
    private final SourceDocumentService sourceService;

    public SourceDocumentController(SourceDocumentService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping(
            path = "/api/v1/workspaces/{workspaceId}/sources",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SourceResponse> upload(
            @PathVariable String workspaceId,
            @RequestParam(required = false) String title,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(sourceService.upload(workspaceId, title, file));
    }

    @GetMapping("/api/v1/workspaces/{workspaceId}/sources")
    public ApiResponse<PageResponse<SourceResponse>> list(
            @PathVariable String workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(sourceService.list(workspaceId, page, size));
    }

    @GetMapping("/api/v1/sources/{sourceId}")
    public ApiResponse<SourceResponse> get(@PathVariable String sourceId) {
        return ApiResponse.ok(sourceService.get(sourceId));
    }

    @PostMapping("/api/v1/sources/{sourceId}/approve")
    public ApiResponse<SourceResponse> approve(@PathVariable String sourceId) {
        return ApiResponse.ok(sourceService.approve(sourceId));
    }

    @PostMapping("/api/v1/sources/{sourceId}/reject")
    public ApiResponse<SourceResponse> reject(
            @PathVariable String sourceId,
            @Valid @RequestBody RejectSourceRequest request
    ) {
        return ApiResponse.ok(sourceService.reject(sourceId, request.reason()));
    }
}
