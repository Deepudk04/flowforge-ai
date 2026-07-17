package com.flowforge.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "generated_documents")
public class GeneratedDocument {
    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "document_type", nullable = false, length = 80)
    private String documentType;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GeneratedDocument() {
    }

    public GeneratedDocument(String id, String title, String documentType, String content, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.documentType = documentType;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
