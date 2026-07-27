package com.flowforge.workspaces.service;

import com.flowforge.common.BadRequestException;
import com.flowforge.common.PageResponse;
import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.organizations.repository.OrganizationRepository;
import com.flowforge.tenancy.OrganizationContext;
import com.flowforge.workspaces.dto.WorkspaceDtos.CreateWorkspaceRequest;
import com.flowforge.workspaces.dto.WorkspaceDtos.UpdateWorkspaceRequest;
import com.flowforge.workspaces.dto.WorkspaceDtos.WorkspaceResponse;
import com.flowforge.workspaces.entity.Workspace;
import com.flowforge.workspaces.repository.WorkspaceRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationContext organizationContext;
    private final Clock clock;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            OrganizationRepository organizationRepository,
            OrganizationContext organizationContext
    ) {
        this(workspaceRepository, organizationRepository, organizationContext, Clock.systemUTC());
    }

    WorkspaceService(
            WorkspaceRepository workspaceRepository,
            OrganizationRepository organizationRepository,
            OrganizationContext organizationContext,
            Clock clock
    ) {
        this.workspaceRepository = workspaceRepository;
        this.organizationRepository = organizationRepository;
        this.organizationContext = organizationContext;
        this.clock = clock;
    }

    @Transactional
    public WorkspaceResponse create(CreateWorkspaceRequest request) {
        var organizationId = organizationContext.organizationId();
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        Instant now = clock.instant();
        var workspace = new Workspace(
                "ws_" + UUID.randomUUID(),
                organizationId,
                request.name().trim(),
                normalize(request.description()),
                organizationContext.actorId(),
                now
        );
        return toResponse(workspaceRepository.save(workspace));
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse get(String workspaceId) {
        return toResponse(requireWorkspace(workspaceId));
    }

    @Transactional(readOnly = true)
    public PageResponse<WorkspaceResponse> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("Page must be non-negative and size must be between 1 and 100");
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));
        var result = workspaceRepository.findAllByOrganizationId(organizationContext.organizationId(), pageable);
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public WorkspaceResponse update(String workspaceId, UpdateWorkspaceRequest request) {
        if (request.name() == null && request.description() == null && request.status() == null) {
            throw new BadRequestException("At least one workspace field must be provided");
        }
        if (request.name() != null && request.name().isBlank()) {
            throw new BadRequestException("Workspace name must not be blank");
        }
        var workspace = requireWorkspace(workspaceId);
        workspace.update(
                request.name() == null ? null : request.name().trim(),
                normalize(request.description()),
                request.status(),
                clock.instant()
        );
        return toResponse(workspaceRepository.save(workspace));
    }

    public Workspace requireWorkspace(String workspaceId) {
        return workspaceRepository.findByIdAndOrganizationId(workspaceId, organizationContext.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private WorkspaceResponse toResponse(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getStatus(),
                workspace.getCreatedBy(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }
}
