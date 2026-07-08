package com.flowforge.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private String status;

    @Column(name = "resource_type", nullable = false, length = 40)
    private String resourceType;

    @Column(name = "resource_id", length = 80)
    private String resourceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DocumentGenerationJob() {
    }

    public DocumentGenerationJob(String id, String status, String resourceType, String resourceId, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}