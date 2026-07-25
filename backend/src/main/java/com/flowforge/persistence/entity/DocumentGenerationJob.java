package com.flowforge.persistence.entity;

import com.flowforge.jobs.model.GenerationJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "generation_jobs")
public class DocumentGenerationJob {
    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private GenerationJobStatus status;

    @Column(name = "resource_type", nullable = false, length = 40)
    private String resourceType;

    @Column(name = "resource_id", length = 80)
    private String resourceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "failure_message", columnDefinition = "text")
    private String failureMessage;

    protected DocumentGenerationJob() {
    }

    public DocumentGenerationJob(
            String id,
            GenerationJobStatus status,
            String resourceType,
            String resourceId,
            Instant createdAt
    ) {
        this.id = id;
        this.status = status;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public GenerationJobStatus getStatus() {
        return status;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void markRunning(Instant updatedAt) {
        transitionTo(GenerationJobStatus.RUNNING, updatedAt);
    }

    public void markCompleted(String resourceId, Instant updatedAt) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("Completed generation jobs require a resource ID.");
        }
        transitionTo(GenerationJobStatus.COMPLETED, updatedAt);
        this.resourceId = resourceId;
        this.failureMessage = null;
    }

    public void markFailed(String failureMessage, Instant updatedAt) {
        transitionTo(GenerationJobStatus.FAILED, updatedAt);
        this.failureMessage = failureMessage;
    }

    private void transitionTo(GenerationJobStatus target, Instant transitionTime) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid generation job transition: " + status + " -> " + target);
        }
        this.status = target;
        this.updatedAt = transitionTime;
    }
}
