package com.flowforge.jobs.service;

import com.flowforge.jobs.dto.GenerationJobDtos.GenerationJobResponse;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class GenerationJobService {
    public GenerationJobResponse getJob(String jobId) {
        var now = Instant.now();
        return new GenerationJobResponse(jobId, "COMPLETED", "document", "SampleDocument", now, now);
    }
}