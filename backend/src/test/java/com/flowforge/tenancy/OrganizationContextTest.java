package com.flowforge.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class OrganizationContextTest {
    private final OrganizationContext context = new OrganizationContext();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void localModeUsesSyntheticDemoIdentity() {
        assertThat(context.organizationId()).isEqualTo(OrganizationContext.DEMO_ORGANIZATION_ID);
        assertThat(context.actorId()).isEqualTo(OrganizationContext.DEMO_ACTOR_ID);
    }

    @Test
    void securedModeReadsTenantAndActorFromJwt() {
        authenticate(Map.of("organization_id", "org_a"), "user_a");

        assertThat(context.organizationId()).isEqualTo("org_a");
        assertThat(context.actorId()).isEqualTo("user_a");
    }

    @Test
    void securedModeRejectsJwtWithoutOrganizationClaim() {
        authenticate(Map.of(), "user_a");

        assertThatThrownBy(context::organizationId)
                .isInstanceOf(MissingOrganizationClaimException.class)
                .hasMessageContaining("organization_id");
    }

    private static void authenticate(Map<String, Object> claims, String subject) {
        var jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.parse("2026-07-27T06:00:00Z"))
                .expiresAt(Instant.parse("2026-07-27T07:00:00Z"));
        claims.forEach(jwt::claim);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt.build()));
    }
}
