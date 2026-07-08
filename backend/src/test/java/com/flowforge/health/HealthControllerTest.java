package com.flowforge.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthControllerTest {
    @Test
    void healthReturnsServiceMetadata() {
        var controller = new HealthController("flowforge-backend");

        var response = controller.health();

        assertThat(response.status()).isEqualTo("ok");
        assertThat(response.service()).isEqualTo("flowforge-backend");
        assertThat(response.timestamp()).isNotNull();
    }
}