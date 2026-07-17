package com.flowforge.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.entity.GeneratedDocument;
import com.flowforge.persistence.entity.WorkflowDiagram;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import com.flowforge.persistence.repository.GeneratedDocumentRepository;
import com.flowforge.persistence.repository.WorkflowDiagramRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GenerationPersistencePostgresTest {
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
    private DocumentGenerationJobRepository jobRepository;

    @Autowired
    private GeneratedDocumentRepository documentRepository;

    @Autowired
    private WorkflowDiagramRepository workflowRepository;

    @Test
    void flywaySchemaSupportsGenerationPersistence() {
        var now = Instant.now();
        var document = documentRepository.save(new GeneratedDocument(
                "doc_123",
                "SampleDocument",
                "document",
                "# SampleDocument",
                now
        ));
        var workflow = workflowRepository.save(new WorkflowDiagram(
                "workflow_123",
                "SampleWorkflow",
                "flowchart TD",
                "Skipping edge to unknown node: missing",
                now
        ));
        var job = new DocumentGenerationJob("job_123", "PENDING", "document", null, now);
        job.markCompleted(document.getId(), now);
        jobRepository.save(job);

        assertThat(documentRepository.findById(document.getId())).isPresent();
        assertThat(workflowRepository.findById(workflow.getId()))
                .get()
                .extracting(WorkflowDiagram::getWarnings)
                .isEqualTo("Skipping edge to unknown node: missing");
        assertThat(jobRepository.findFirstByResourceTypeAndResourceIdOrderByCreatedAtDesc("document", "doc_123"))
                .get()
                .extracting(DocumentGenerationJob::getStatus)
                .isEqualTo("COMPLETED");
    }
}
