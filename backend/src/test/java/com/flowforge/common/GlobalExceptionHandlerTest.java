package com.flowforge.common;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void badRequestReturnsStandardErrorEnvelope() {
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/documents/generate");

        var response = handler.handleBadRequest(new BadRequestException("Invalid request"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().code()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().error().path()).isEqualTo("/api/v1/documents/generate");
    }
}