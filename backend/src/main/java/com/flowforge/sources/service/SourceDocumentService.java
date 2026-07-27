package com.flowforge.sources.service;

import com.flowforge.common.BadRequestException;
import com.flowforge.common.ConflictException;
import com.flowforge.common.PageResponse;
import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.sources.dto.SourceDtos.SourceResponse;
import com.flowforge.sources.entity.SourceDocument;
import com.flowforge.sources.model.SourceStatus;
import com.flowforge.sources.repository.SourceDocumentRepository;
import com.flowforge.sources.storage.SourceStorage;
import com.flowforge.tenancy.OrganizationContext;
import com.flowforge.workspaces.model.WorkspaceStatus;
import com.flowforge.workspaces.service.WorkspaceService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SourceDocumentService {
    private final SourceDocumentRepository sourceRepository;
    private final WorkspaceService workspaceService;
    private final OrganizationContext organizationContext;
    private final SourceContentExtractor contentExtractor;
    private final SourceStorage sourceStorage;
    private final Clock clock;

    public SourceDocumentService(
            SourceDocumentRepository sourceRepository,
            WorkspaceService workspaceService,
            OrganizationContext organizationContext,
            SourceContentExtractor contentExtractor,
            SourceStorage sourceStorage
    ) {
        this(
                sourceRepository,
                workspaceService,
                organizationContext,
                contentExtractor,
                sourceStorage,
                Clock.systemUTC()
        );
    }

    SourceDocumentService(
            SourceDocumentRepository sourceRepository,
            WorkspaceService workspaceService,
            OrganizationContext organizationContext,
            SourceContentExtractor contentExtractor,
            SourceStorage sourceStorage,
            Clock clock
    ) {
        this.sourceRepository = sourceRepository;
        this.workspaceService = workspaceService;
        this.organizationContext = organizationContext;
        this.contentExtractor = contentExtractor;
        this.sourceStorage = sourceStorage;
        this.clock = clock;
    }

    @Transactional
    public SourceResponse upload(String workspaceId, String title, MultipartFile file) {
        var workspace = workspaceService.requireWorkspace(workspaceId);
        if (workspace.getStatus() != WorkspaceStatus.ACTIVE) {
            throw new ConflictException("Sources cannot be added to an archived workspace");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.length() > 255) {
            throw new BadRequestException("Source filename is required and must not exceed 255 characters");
        }
        byte[] bytes = bytes(file);
        var extracted = contentExtractor.extract(filename, file.getContentType(), bytes);
        String normalizedTitle = normalizeTitle(title, filename);
        String contentHash = sha256(extracted.content());
        String organizationId = organizationContext.organizationId();

        if (sourceRepository.existsByOrganizationIdAndWorkspaceIdAndContentHash(
                organizationId,
                workspaceId,
                contentHash
        )) {
            throw new ConflictException("An identical source already exists in this workspace");
        }

        String sourceId = "src_" + UUID.randomUUID();
        String storagePath = sourceStorage.store(organizationId, workspaceId, sourceId, filename, bytes);
        Instant now = clock.instant();
        var source = new SourceDocument(
                sourceId,
                organizationId,
                workspaceId,
                normalizedTitle,
                filename,
                extracted.sourceType(),
                extracted.contentType(),
                storagePath,
                extracted.content(),
                contentHash,
                bytes.length,
                organizationContext.actorId(),
                now
        );
        try {
            return toResponse(sourceRepository.saveAndFlush(source));
        } catch (DataIntegrityViolationException exception) {
            sourceStorage.delete(storagePath);
            throw new ConflictException("An identical source already exists in this workspace");
        } catch (RuntimeException exception) {
            sourceStorage.delete(storagePath);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public SourceResponse get(String sourceId) {
        return toResponse(requireSource(sourceId));
    }

    @Transactional(readOnly = true)
    public PageResponse<SourceResponse> list(String workspaceId, int page, int size) {
        workspaceService.requireWorkspace(workspaceId);
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("Page must be non-negative and size must be between 1 and 100");
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));
        var result = sourceRepository.findAllByOrganizationIdAndWorkspaceId(
                organizationContext.organizationId(),
                workspaceId,
                pageable
        );
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public SourceResponse approve(String sourceId) {
        var source = requireSource(sourceId);
        source.approve(organizationContext.actorId(), clock.instant());
        return toResponse(sourceRepository.save(source));
    }

    @Transactional
    public SourceResponse reject(String sourceId, String reason) {
        var source = requireSource(sourceId);
        source.reject(reason.trim(), clock.instant());
        return toResponse(sourceRepository.save(source));
    }

    @Transactional(readOnly = true)
    public List<SourceDocument> approvedSources(String workspaceId) {
        workspaceService.requireWorkspace(workspaceId);
        return sourceRepository.findAllByOrganizationIdAndWorkspaceIdAndStatusOrderByCreatedAtAscIdAsc(
                organizationContext.organizationId(),
                workspaceId,
                SourceStatus.APPROVED
        );
    }

    private SourceDocument requireSource(String sourceId) {
        return sourceRepository.findByIdAndOrganizationId(sourceId, organizationContext.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Source not found"));
    }

    private byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException("Source file could not be read");
        }
    }

    private String normalizeTitle(String title, String filename) {
        String value = title == null || title.isBlank() ? filename : title.trim();
        if (value.length() > 200) {
            throw new BadRequestException("Source title must not exceed 200 characters");
        }
        return value;
    }

    private String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private SourceResponse toResponse(SourceDocument source) {
        return new SourceResponse(
                source.getId(),
                source.getWorkspaceId(),
                source.getTitle(),
                source.getOriginalFilename(),
                source.getSourceType(),
                source.getContentType(),
                source.getContentHash(),
                source.getSizeBytes(),
                source.getStatus(),
                source.getApprovedBy(),
                source.getApprovedAt(),
                source.getRejectionReason(),
                source.getCreatedBy(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }
}
