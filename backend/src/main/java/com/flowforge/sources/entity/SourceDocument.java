package com.flowforge.sources.entity;

import com.flowforge.common.ConflictException;
import com.flowforge.sources.model.SourceStatus;
import com.flowforge.sources.model.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "source_documents")
public class SourceDocument {
    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "organization_id", nullable = false, length = 80)
    private String organizationId;

    @Column(name = "workspace_id", nullable = false, length = 80)
    private String workspaceId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private SourceType sourceType;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "extracted_content", nullable = false, columnDefinition = "text")
    private String extractedContent;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private SourceStatus status;

    @Column(name = "approved_by", length = 160)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    @Column(name = "failure_message", columnDefinition = "text")
    private String failureMessage;

    @Column(name = "created_by", nullable = false, length = 160)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SourceDocument() {
    }

    public SourceDocument(
            String id,
            String organizationId,
            String workspaceId,
            String title,
            String originalFilename,
            SourceType sourceType,
            String contentType,
            String storagePath,
            String extractedContent,
            String contentHash,
            long sizeBytes,
            String createdBy,
            Instant createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.workspaceId = workspaceId;
        this.title = title;
        this.originalFilename = originalFilename;
        this.sourceType = sourceType;
        this.contentType = contentType;
        this.storagePath = storagePath;
        this.extractedContent = extractedContent;
        this.contentHash = contentHash;
        this.sizeBytes = sizeBytes;
        this.status = SourceStatus.UPLOADED;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void approve(String actorId, Instant now) {
        requireUploaded("approve");
        this.status = SourceStatus.APPROVED;
        this.approvedBy = actorId;
        this.approvedAt = now;
        this.rejectionReason = null;
        this.updatedAt = now;
    }

    public void reject(String reason, Instant now) {
        requireUploaded("reject");
        this.status = SourceStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = now;
    }

    private void requireUploaded(String action) {
        if (status != SourceStatus.UPLOADED) {
            throw new ConflictException("Cannot " + action + " source in status " + status);
        }
    }

    public String getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getExtractedContent() {
        return extractedContent;
    }

    public String getContentHash() {
        return contentHash;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public SourceStatus getStatus() {
        return status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getFailureMessage() {
        return failureMessage;
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
