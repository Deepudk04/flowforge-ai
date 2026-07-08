package com.flowforge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI flowForgeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FlowForge Backend API")
                .version("v1")
                .description("APIs for document generation, workflow diagrams, and generation jobs."));
    }
}