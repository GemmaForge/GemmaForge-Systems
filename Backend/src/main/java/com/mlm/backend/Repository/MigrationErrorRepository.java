package com.mlm.backend.Repository;

import com.mlm.backend.Model.MigrationError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MigrationErrorRepository extends JpaRepository<MigrationError, Long> {
    Optional<MigrationError> findByErrorCode(String errorCode);

    // Allows us to search the knowledge base using fragments of a raw compiler log
    List<MigrationError> findByErrorSnippetContainingIgnoreCase(String snippet);
}