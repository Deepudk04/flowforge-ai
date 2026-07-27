package com.flowforge.sources.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flowforge.common.ConflictException;
import com.flowforge.sources.entity.SourceDocument;
import com.flowforge.sources.model.SourceStatus;
import com.flowforge.sources.model.SourceType;
import com.flowforge.sources.repository.SourceDocumentRepository;
import com.flowforge.sources.storage.SourceStorage;
import com.flowforge.tenancy.OrganizationContext;
import com.flowforge.workspaces.entity.Workspace;
import com.flowforge.workspaces.service.WorkspaceService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

class SourceDocumentServiceTest {
    private static final Instant NOW = Instant.parse("2026-07-27T06:00:00Z");

    @Test
    void uploadPersistsExtractedSourceAndContentHash() {
        var fixture = fixture();
        var file = new MockMultipartFile(
                "file",
                "guide.md",
                "text/markdown",
                "# Synthetic guide".getBytes(StandardCharsets.UTF_8)
        );
        when(fixture.extractor.extract(eq("guide.md"), eq("text/markdown"), any(byte[].class)))
                .thenReturn(new SourceContentExtractor.ExtractedSource(
                        SourceType.MARKDOWN,
                        "text/markdown",
                        "# Synthetic guide"
                ));
        when(fixture.repository.existsByOrganizationIdAndWorkspaceIdAndContentHash(
                eq("org_a"),
                eq("ws_123"),
                anyString()
        )).thenReturn(false);
        when(fixture.storage.store(eq("org_a"), eq("ws_123"), anyString(), eq("guide.md"), any(byte[].class)))
                .thenReturn("org_a/ws_123/source/guide.md");
        when(fixture.repository.saveAndFlush(any(SourceDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = fixture.service.upload("ws_123", "  Operations Guide  ", file);

        assertThat(response.title()).isEqualTo("Operations Guide");
        assertThat(response.status()).isEqualTo(SourceStatus.UPLOADED);
        assertThat(response.contentHash()).hasSize(64);
        var source = ArgumentCaptor.forClass(SourceDocument.class);
        verify(fixture.repository).saveAndFlush(source.capture());
        assertThat(source.getValue().getOrganizationId()).isEqualTo("org_a");
        assertThat(source.getValue().getCreatedBy()).isEqualTo("user_a");
        assertThat(source.getValue().getExtractedContent()).isEqualTo("# Synthetic guide");
    }

    @Test
    void duplicateSourceIsRejectedBeforeStorage() {
        var fixture = fixture();
        var bytes = "same content".getBytes(StandardCharsets.UTF_8);
        var file = new MockMultipartFile("file", "same.txt", "text/plain", bytes);
        when(fixture.extractor.extract("same.txt", "text/plain", bytes))
                .thenReturn(new SourceContentExtractor.ExtractedSource(SourceType.TEXT, "text/plain", "same content"));
        when(fixture.repository.existsByOrganizationIdAndWorkspaceIdAndContentHash(
                eq("org_a"),
                eq("ws_123"),
                anyString()
        )).thenReturn(true);

        assertThatThrownBy(() -> fixture.service.upload("ws_123", null, file))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("identical source");

        verify(fixture.storage, never()).store(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void storageIsCleanedUpWhenConcurrentDuplicateWins() {
        var fixture = fixture();
        var bytes = "same content".getBytes(StandardCharsets.UTF_8);
        var file = new MockMultipartFile("file", "same.txt", "text/plain", bytes);
        when(fixture.extractor.extract("same.txt", "text/plain", bytes))
                .thenReturn(new SourceContentExtractor.ExtractedSource(SourceType.TEXT, "text/plain", "same content"));
        when(fixture.storage.store(eq("org_a"), eq("ws_123"), anyString(), eq("same.txt"), eq(bytes)))
                .thenReturn("org_a/ws_123/src_new/same.txt");
        when(fixture.repository.saveAndFlush(any(SourceDocument.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> fixture.service.upload("ws_123", null, file))
                .isInstanceOf(ConflictException.class);

        verify(fixture.storage).delete("org_a/ws_123/src_new/same.txt");
    }

    @Test
    void generationSelectionRequestsOnlyApprovedSourcesForCurrentTenant() {
        var fixture = fixture();
        when(fixture.repository.findAllByOrganizationIdAndWorkspaceIdAndStatusOrderByCreatedAtAscIdAsc(
                "org_a",
                "ws_123",
                SourceStatus.APPROVED
        )).thenReturn(List.of());

        assertThat(fixture.service.approvedSources("ws_123")).isEmpty();

        verify(fixture.repository).findAllByOrganizationIdAndWorkspaceIdAndStatusOrderByCreatedAtAscIdAsc(
                "org_a",
                "ws_123",
                SourceStatus.APPROVED
        );
    }

    private static Fixture fixture() {
        var repository = mock(SourceDocumentRepository.class);
        var workspaceService = mock(WorkspaceService.class);
        var organizationContext = mock(OrganizationContext.class);
        var extractor = mock(SourceContentExtractor.class);
        var storage = mock(SourceStorage.class);
        var workspace = new Workspace("ws_123", "org_a", "Workspace", null, "user_a", NOW);
        when(workspaceService.requireWorkspace("ws_123")).thenReturn(workspace);
        when(organizationContext.organizationId()).thenReturn("org_a");
        when(organizationContext.actorId()).thenReturn("user_a");
        var service = new SourceDocumentService(
                repository,
                workspaceService,
                organizationContext,
                extractor,
                storage,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        return new Fixture(service, repository, extractor, storage);
    }

    private record Fixture(
            SourceDocumentService service,
            SourceDocumentRepository repository,
            SourceContentExtractor extractor,
            SourceStorage storage
    ) {
    }
}
