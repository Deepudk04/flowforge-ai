package com.flowforge.persistence.repository;

import com.flowforge.persistence.entity.DocumentGenerationJob;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentGenerationJobRepository extends JpaRepository<DocumentGenerationJob, String> {
    Optional<DocumentGenerationJob> findFirstByResourceTypeAndResourceIdOrderByCreatedAtDesc(
            String resourceType,
            String resourceId
    );
}
