package com.flowforge.tenancy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class OrganizationContext {
    public static final String DEMO_ORGANIZATION_ID = "org_demo";
    public static final String DEMO_ACTOR_ID = "demo-user";

    public String organizationId() {
        var jwt = jwt();
        if (jwt == null) {
            return DEMO_ORGANIZATION_ID;
        }
        var organizationId = jwt.getClaimAsString("organization_id");
        if (organizationId == null || organizationId.isBlank()) {
            throw new MissingOrganizationClaimException();
        }
        return organizationId;
    }

    public String actorId() {
        var jwt = jwt();
        return jwt == null ? DEMO_ACTOR_ID : jwt.getSubject();
    }

    private Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt;
    }
}
