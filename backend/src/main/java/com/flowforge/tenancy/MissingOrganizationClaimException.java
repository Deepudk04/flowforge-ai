package com.flowforge.tenancy;

import com.flowforge.common.BadRequestException;

public class MissingOrganizationClaimException extends BadRequestException {
    public MissingOrganizationClaimException() {
        super("Authenticated user is missing the organization_id claim");
    }
}
