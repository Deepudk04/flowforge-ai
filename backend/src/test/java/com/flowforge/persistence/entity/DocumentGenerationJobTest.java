package com.flowforge.persistence.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flowforge.jobs.model.GenerationJobStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DocumentGenerationJobTest {
    @Test
    void rejectsSkippingFromPendingToCompleted() {
        var job = new DocumentGenerationJob(
                "job_123",
                GenerationJobStatus.PENDING,
                "document",
                null,
                Instant.now()
        );

        assertThatThrownBy(() -> job.markCompleted("doc_123", Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING -> COMPLETED");
    }

    @Test
    void rejectsTransitionFromTerminalState() {
        var job = new DocumentGenerationJob(
                "job_123",
                GenerationJobStatus.PENDING,
                "document",
                null,
                Instant.now()
        );
        job.markRunning(Instant.now());
        job.markFailed("Synthetic failure", Instant.now());

        assertThatThrownBy(() -> job.markRunning(Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FAILED -> RUNNING");
    }
}
