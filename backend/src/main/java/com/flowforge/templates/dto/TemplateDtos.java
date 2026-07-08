package com.flowforge.templates.dto;

import java.util.List;

public final class TemplateDtos {
    private TemplateDtos() {
    }

    public record TemplateResponse(
            String templateId,
            String name,
            String documentType,
            String version,
            List<String> tags
    ) {
    }
}