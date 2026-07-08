package com.flowforge.common;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        String path,
        String correlationId,
        Instant timestamp,
        List<ValidationError> validationErrors
) {
    public record ValidationError(String field, String message) {
    }
}