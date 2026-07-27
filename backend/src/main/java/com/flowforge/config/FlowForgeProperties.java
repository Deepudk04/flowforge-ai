package com.flowforge.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "flowforge")
public record FlowForgeProperties(
        @NotBlank String apiBasePath,
        @NotBlank String frontendOrigin,
        @Valid @NotNull Security security,
        @Valid @NotNull AiService aiService,
        @Valid @NotNull Sources sources
) {
    public record Security(
            boolean enabled,
            @NotBlank String audience
    ) {
    }

    public record AiService(
            @NotBlank String mode,
            @NotBlank String baseUrl,
            @Min(1000) int timeoutMs,
            @Min(1) int retryAttempts,
            @Min(0) long retryBackoffMs
    ) {
    }

    public record Sources(
            @NotNull Path storageRoot,
            @Min(1) long maxFileSizeBytes,
            @Min(1) int maxPdfPages
    ) {
    }
}
