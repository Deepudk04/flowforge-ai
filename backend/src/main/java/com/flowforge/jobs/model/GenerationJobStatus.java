package com.flowforge.jobs.model;

import java.util.EnumSet;
import java.util.Set;

public enum GenerationJobStatus {
    PENDING,
    RUNNING,
    RETRY_SCHEDULED,
    REVIEW_REQUIRED,
    COMPLETED,
    FAILED,
    CANCELLED;

    public boolean canTransitionTo(GenerationJobStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<GenerationJobStatus> allowedTargets() {
        return switch (this) {
            case PENDING -> EnumSet.of(RUNNING, FAILED, CANCELLED);
            case RUNNING -> EnumSet.of(RETRY_SCHEDULED, REVIEW_REQUIRED, COMPLETED, FAILED, CANCELLED);
            case RETRY_SCHEDULED -> EnumSet.of(RUNNING, FAILED, CANCELLED);
            case REVIEW_REQUIRED -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, FAILED, CANCELLED -> EnumSet.noneOf(GenerationJobStatus.class);
        };
    }
}
