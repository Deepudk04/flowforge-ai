package com.flowforge.sources.repository;

import com.flowforge.sources.entity.SourceDocument;
import com.flowforge.sources.model.SourceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, String> {
    Optional<SourceDocument> findByIdAndOrganizationId(String id, String organizationId);

    Page<SourceDocument> findAllByOrganizationIdAndWorkspaceId(
            String organizationId,
            String workspaceId,
            Pageable pageable
    );

    boolean existsByOrganizationIdAndWorkspaceIdAndContentHash(
            String organizationId,
            String workspaceId,
            String contentHash
    );

    List<SourceDocument> findAllByOrganizationIdAndWorkspaceIdAndStatusOrderByCreatedAtAscIdAsc(
            String organizationId,
            String workspaceId,
            SourceStatus status
    );
}
