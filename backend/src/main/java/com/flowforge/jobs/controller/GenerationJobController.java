package com.flowforge.jobs.controller;

import com.flowforge.common.ApiResponse;
import com.flowforge.jobs.dto.GenerationJobDtos.GenerationJobResponse;
import com.flowforge.jobs.service.GenerationJobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/generation-jobs")
public class GenerationJobController {
    private final GenerationJobService service;

    public GenerationJobController(GenerationJobService service) {
        this.service = service;
    }

    @GetMapping("/{jobId}")
    public ApiResponse<GenerationJobResponse> getJob(@PathVariable String jobId) {
        return ApiResponse.ok(service.getJob(jobId));
    }
}