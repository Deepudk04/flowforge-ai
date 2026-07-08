package com.flowforge.persistence.repository;

import com.flowforge.persistence.entity.WorkflowDiagram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowDiagramRepository extends JpaRepository<WorkflowDiagram, String> {
}