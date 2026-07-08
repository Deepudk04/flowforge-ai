package com.flowforge.templates.controller;

import com.flowforge.common.ApiResponse;
import com.flowforge.templates.dto.TemplateDtos.TemplateResponse;
import com.flowforge.templates.service.TemplateRegistryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {
    private final TemplateRegistryService service;

    public TemplateController(TemplateRegistryService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<TemplateResponse>> listTemplates() {
        return ApiResponse.ok(service.listTemplates());
    }
}