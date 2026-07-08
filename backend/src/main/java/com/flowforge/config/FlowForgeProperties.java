package com.flowforge.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "flowforge")
public record FlowForgeProperties(
        @NotBlank String apiBasePath,
        @NotBlank String frontendOrigin,
        AiService aiService
) {
    public record AiService(
            @NotBlank String baseUrl,
            @Min(1000) int timeoutMs
    ) {
    }
}