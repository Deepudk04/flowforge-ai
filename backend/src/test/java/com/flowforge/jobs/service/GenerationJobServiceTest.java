package com.flowforge.jobs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.flowforge.common.ResourceNotFoundException;
import com.flowforge.persistence.entity.DocumentGenerationJob;
import com.flowforge.persistence.repository.DocumentGenerationJobRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GenerationJobServiceTest {
    @Test
    void getJobReadsPersistedJob() {
        var repository = mock(DocumentGenerationJobRepository.class);
        var job = new DocumentGenerationJob("job_123", "PENDING", "document", null, Instant.now());
        job.markCompleted("doc_123", Instant.now());
        when(repository.findById("job_123")).thenReturn(Optional.of(job));
        var service = new GenerationJobService(repository);

        var response = service.getJob("job_123");

        assertThat(response.jobId()).isEqualTo("job_123");
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.resourceType()).isEqualTo("document");
        assertThat(response.resourceId()).isEqualTo("doc_123");
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void getJobThrowsWhenAbsent() {
        var repository = mock(DocumentGenerationJobRepository.class);
        when(repository.findById("missing")).thenReturn(Optional.empty());
        var service = new GenerationJobService(repository);

        assertThatThrownBy(() -> service.getJob("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
