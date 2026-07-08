package com.flowforge.workflows.controller;

import com.flowforge.common.ApiResponse;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramRequest;
import com.flowforge.workflows.dto.WorkflowDtos.WorkflowDiagramResponse;
import com.flowforge.workflows.service.WorkflowDiagramService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowDiagramController {
    private final WorkflowDiagramService service;

    public WorkflowDiagramController(WorkflowDiagramService service) {
        this.service = service;
    }

    @PostMapping("/diagram")
    public ApiResponse<WorkflowDiagramResponse> generate(@Valid @RequestBody WorkflowDiagramRequest request) {
        return ApiResponse.ok(service.generate(request));
    }
}