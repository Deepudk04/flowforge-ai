package com.flowforge.sources.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flowforge.common.ConflictException;
import com.flowforge.sources.model.SourceStatus;
import com.flowforge.sources.model.SourceType;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SourceDocumentTest {
    private static final Instant CREATED_AT = Instant.parse("2026-07-27T06:00:00Z");

    @Test
    void uploadedSourceCanBeApproved() {
        var source = source();
        var approvedAt = CREATED_AT.plusSeconds(60);

        source.approve("reviewer", approvedAt);

        assertThat(source.getStatus()).isEqualTo(SourceStatus.APPROVED);
        assertThat(source.getApprovedBy()).isEqualTo("reviewer");
        assertThat(source.getApprovedAt()).isEqualTo(approvedAt);
        assertThat(source.getUpdatedAt()).isEqualTo(approvedAt);
    }

    @Test
    void uploadedSourceCanBeRejected() {
        var source = source();

        source.reject("Outdated material", CREATED_AT.plusSeconds(60));

        assertThat(source.getStatus()).isEqualTo(SourceStatus.REJECTED);
        assertThat(source.getRejectionReason()).isEqualTo("Outdated material");
    }

    @Test
    void terminalReviewDecisionCannotBeChanged() {
        var source = source();
        source.approve("reviewer", CREATED_AT.plusSeconds(60));

        assertThatThrownBy(() -> source.reject("Changed mind", CREATED_AT.plusSeconds(120)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("APPROVED");
    }

    private static SourceDocument source() {
        return new SourceDocument(
                "src_123",
                "org_demo",
                "ws_123",
                "Guide",
                "guide.md",
                SourceType.MARKDOWN,
                "text/markdown",
                "org_demo/ws_123/src_123/guide.md",
                "# Guide",
                "a".repeat(64),
                7,
                "author",
                CREATED_AT
        );
    }
}
