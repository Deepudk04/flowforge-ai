package com.flowforge.organizations.repository;

import com.flowforge.organizations.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, String> {
}
