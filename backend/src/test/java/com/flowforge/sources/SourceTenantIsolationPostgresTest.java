package com.flowforge.sources;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowforge.organizations.entity.Organization;
import com.flowforge.organizations.repository.OrganizationRepository;
import com.flowforge.sources.entity.SourceDocument;
import com.flowforge.sources.model.SourceType;
import com.flowforge.sources.repository.SourceDocumentRepository;
import com.flowforge.workspaces.entity.Workspace;
import com.flowforge.workspaces.repository.WorkspaceRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SourceTenantIsolationPostgresTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("flowforge")
            .withUsername("flowforge")
            .withPassword("flowforge");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private SourceDocumentRepository sourceRepository;

    @Test
    void workspaceAndSourceQueriesDoNotCrossOrganizationBoundary() {
        var now = Instant.parse("2026-07-27T06:00:00Z");
        organizationRepository.save(new Organization("org_a", "Organization A", now));
        organizationRepository.save(new Organization("org_b", "Organization B", now));
        workspaceRepository.save(new Workspace("ws_a", "org_a", "Workspace A", null, "user_a", now));
        workspaceRepository.save(new Workspace("ws_b", "org_b", "Workspace B", null, "user_b", now));
        sourceRepository.save(source("src_a", "org_a", "ws_a", now));
        sourceRepository.save(source("src_b", "org_b", "ws_b", now));

        assertThat(workspaceRepository.findByIdAndOrganizationId("ws_b", "org_a")).isEmpty();
        assertThat(workspaceRepository.findAllByOrganizationId("org_a", PageRequest.of(0, 20)).getContent())
                .extracting(Workspace::getId)
                .containsExactly("ws_a");
        assertThat(sourceRepository.findByIdAndOrganizationId("src_b", "org_a")).isEmpty();
        assertThat(sourceRepository.findAllByOrganizationIdAndWorkspaceId(
                "org_a",
                "ws_a",
                PageRequest.of(0, 20)
        ).getContent())
                .extracting(SourceDocument::getId)
                .containsExactly("src_a");
    }

    private static SourceDocument source(String id, String organizationId, String workspaceId, Instant now) {
        return new SourceDocument(
                id,
                organizationId,
                workspaceId,
                "Synthetic guide",
                "guide.md",
                SourceType.MARKDOWN,
                "text/markdown",
                organizationId + "/" + workspaceId + "/" + id + "/guide.md",
                "# Synthetic guide",
                ("a".repeat(63) + id.substring(id.length() - 1)),
                17,
                "author",
                now
        );
    }
}
