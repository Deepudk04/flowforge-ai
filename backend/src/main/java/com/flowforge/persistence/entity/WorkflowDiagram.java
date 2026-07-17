package com.flowforge.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workflow_diagrams")
public class WorkflowDiagram {
    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "mermaid", nullable = false, columnDefinition = "text")
    private String mermaid;

    @Column(name = "warnings", columnDefinition = "text")
    private String warnings;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WorkflowDiagram() {
    }

    public WorkflowDiagram(String id, String title, String mermaid, String warnings, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.mermaid = mermaid;
        this.warnings = warnings;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMermaid() {
        return mermaid;
    }

    public String getWarnings() {
        return warnings;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
