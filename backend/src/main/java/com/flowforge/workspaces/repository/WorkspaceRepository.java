package com.flowforge.workspaces.repository;

import com.flowforge.workspaces.entity.Workspace;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    Optional<Workspace> findByIdAndOrganizationId(String id, String organizationId);

    Page<Workspace> findAllByOrganizationId(String organizationId, Pageable pageable);
}
