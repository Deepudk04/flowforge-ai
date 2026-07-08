package com.flowforge.persistence.repository;

import com.flowforge.persistence.entity.DocumentGenerationJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentGenerationJobRepository extends JpaRepository<DocumentGenerationJob, String> {
}