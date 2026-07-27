package com.flowforge.workspaces.entity;

import com.flowforge.workspaces.model.WorkspaceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workspaces")
public class Workspace {
    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "organization_id", nullable = false, length = 80)
    private String organizationId;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private WorkspaceStatus status;

    @Column(name = "created_by", nullable = false, length = 160)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Workspace() {
    }

    public Workspace(
            String id,
            String organizationId,
            String name,
            String description,
            String createdBy,
            Instant createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.status = WorkspaceStatus.ACTIVE;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void update(String name, String description, WorkspaceStatus status, Instant updatedAt) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
