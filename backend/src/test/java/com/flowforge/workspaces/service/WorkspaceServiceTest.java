package com.flowforge.workspaces.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.common.BadRequestException;
import com.flowforge.organizations.repository.OrganizationRepository;
import com.flowforge.tenancy.OrganizationContext;
import com.flowforge.workspaces.dto.WorkspaceDtos.UpdateWorkspaceRequest;
import com.flowforge.workspaces.repository.WorkspaceRepository;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WorkspaceServiceTest {
    @Test
    void lookupIsScopedToCurrentOrganization() {
        var repository = mock(WorkspaceRepository.class);
        var context = mock(OrganizationContext.class);
        when(context.organizationId()).thenReturn("org_a");
        when(repository.findByIdAndOrganizationId("ws_other", "org_a")).thenReturn(Optional.empty());
        var service = new WorkspaceService(
                repository,
                mock(OrganizationRepository.class),
                context,
                Clock.systemUTC()
        );

        assertThatThrownBy(() -> service.get("ws_other"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workspace not found");

        verify(repository).findByIdAndOrganizationId("ws_other", "org_a");
    }

    @Test
    void updateRejectsWhitespaceOnlyName() {
        var service = new WorkspaceService(
                mock(WorkspaceRepository.class),
                mock(OrganizationRepository.class),
                mock(OrganizationContext.class),
                Clock.systemUTC()
        );

        assertThatThrownBy(() -> service.update(
                "ws_123",
                new UpdateWorkspaceRequest("   ", null, null)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be blank");
    }
}
