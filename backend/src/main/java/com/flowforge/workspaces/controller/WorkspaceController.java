package com.flowforge.workspaces.controller;

import com.flowforge.common.ApiResponse;
import com.flowforge.common.PageResponse;
import com.flowforge.workspaces.dto.WorkspaceDtos.CreateWorkspaceRequest;
import com.flowforge.workspaces.dto.WorkspaceDtos.UpdateWorkspaceRequest;
import com.flowforge.workspaces.dto.WorkspaceDtos.WorkspaceResponse;
import com.flowforge.workspaces.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkspaceResponse> create(@Valid @RequestBody CreateWorkspaceRequest request) {
        return ApiResponse.ok(workspaceService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<WorkspaceResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(workspaceService.list(page, size));
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> get(@PathVariable String workspaceId) {
        return ApiResponse.ok(workspaceService.get(workspaceId));
    }

    @PatchMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> update(
            @PathVariable String workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request
    ) {
        return ApiResponse.ok(workspaceService.update(workspaceId, request));
    }
}
