package com.flowforge.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flowforge.health.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "flowforge.security.enabled=false",
        "flowforge.security.audience=flowforge-api",
        "flowforge.api-base-path=/api/v1",
        "flowforge.frontend-origin=http://localhost:5173",
        "flowforge.ai-service.base-url=http://localhost:8000",
        "flowforge.ai-service.timeout-ms=30000"
})
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void localModeAllowsHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }
}