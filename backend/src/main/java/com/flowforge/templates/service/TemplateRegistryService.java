package com.flowforge.templates.service;

import com.flowforge.templates.dto.TemplateDtos.TemplateResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TemplateRegistryService {
    public List<TemplateResponse> listTemplates() {
        return List.of(
                new TemplateResponse("template-document-v1", "Document draft", "document", "v1", List.of("document")),
                new TemplateResponse("template-workflow-v1", "Workflow diagram", "workflow", "v1", List.of("workflow"))
        );
    }
}