package com.flowforge.workspaces.dto;

import com.flowforge.workspaces.model.WorkspaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class WorkspaceDtos {
    private WorkspaceDtos() {
    }

    public record CreateWorkspaceRequest(
            @NotBlank @Size(max = 160) String name,
            @Size(max = 4000) String description
    ) {
    }

    public record UpdateWorkspaceRequest(
            @Size(min = 1, max = 160) String name,
            @Size(max = 4000) String description,
            WorkspaceStatus status
    ) {
    }

    public record WorkspaceResponse(
            String id,
            String name,
            String description,
            WorkspaceStatus status,
            String createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
