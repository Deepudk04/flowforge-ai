package com.flowforge.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LogMaskerTest {
    @Test
    void masksSensitiveValues() {
        assertThat(LogMasker.mask("authorization", "Bearer abc")).isEqualTo("[REDACTED]");
        assertThat(LogMasker.mask("password", "demo")).isEqualTo("[REDACTED]");
        assertThat(LogMasker.mask("x-request-id", "request-1")).isEqualTo("request-1");
    }
}