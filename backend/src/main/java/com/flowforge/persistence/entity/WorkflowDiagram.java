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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WorkflowDiagram() {
    }

    public WorkflowDiagram(String id, String title, String mermaid, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.mermaid = mermaid;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}