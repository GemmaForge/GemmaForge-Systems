package com.mlm.backend.Repository;

import com.mlm.backend.Model.MigrationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MigrationReportRepository extends JpaRepository<MigrationReport, Long> {
    List<MigrationReport> findByProjectId(Long projectId);
    List<MigrationReport> findByStatus(MigrationReport.ReportStatus status);
}