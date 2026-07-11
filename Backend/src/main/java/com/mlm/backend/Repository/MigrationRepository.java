package com.mlm.backend.Repository;

import com.mlm.backend.Model.Migration;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MigrationRepository extends JpaRepository<Migration, Long> {

    Page<Migration> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Migration> findAllByOrderByCreatedAtDesc();
    List<Migration> findByProjectNameIgnoreCase(String projectName);
    List<Migration> findByStatus(Migration.MigrationStatus status);
    List<Migration> findByProjectNameOrderByCreatedAtDesc(String projectName);

    @Query("SELECT COALESCE(SUM(m.wavefrontBugsDetected), 0) FROM Migration m WHERE m.status = 'COMPLETED'")
    Integer calculateTotalWavefrontBugsResolved();

    @Query("SELECT COALESCE(SUM(m.estimatedSavings), 0) FROM Migration m WHERE m.status = 'COMPLETED'")
    Long calculateTotalCorporateSavings();

    @Query("SELECT COUNT(DISTINCT m.projectName) FROM Migration m WHERE m.status = 'COMPLETED'")
    Long countCompletedProjects();

    @Modifying
    @Transactional
    @Query("UPDATE Migration m SET m.status = 'COMPLETED' WHERE m.migrationId = :id")
    void markAsCompleted(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(m.tokensUsed), 0) FROM Migration m")
    Integer calculateTotalTokensUsed();
}