package com.flowforge.persistence.repository;

import com.flowforge.persistence.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, String> {
}