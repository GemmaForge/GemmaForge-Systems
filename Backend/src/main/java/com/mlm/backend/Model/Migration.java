package com.mlm.backend.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "migrations")
public class Migration {

    public enum MigrationStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long migrationId;

    @Column(nullable = false, length = 100)
    private String projectName;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalCudaCode;

    @Column(columnDefinition = "TEXT")
    private String hipifyOutputCode;

    @Column(columnDefinition = "TEXT")
    private String finalRocmCode;

    @Column(nullable = false)
    private int wavefrontBugsDetected = 0;

    @Column(nullable = false)
    private Long estimatedSavings = 0L;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MigrationStatus status = MigrationStatus.PENDING;

    @Column(nullable = false)
    private int tokensUsed = 0;

    @Column(name = "security_audit_passed", nullable = false)
    private boolean securityAuditPassed = false;

    @Column(columnDefinition = "TEXT", name = "vulnerability_report")
    private String vulnerabilityReport;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getMigrationId() { return migrationId; }
    public void setMigrationId(Long migrationId) { this.migrationId = migrationId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getOriginalCudaCode() { return originalCudaCode; }
    public void setOriginalCudaCode(String originalCudaCode) { this.originalCudaCode = originalCudaCode; }

    public String getHipifyOutputCode() { return hipifyOutputCode; }
    public void setHipifyOutputCode(String hipifyOutputCode) { this.hipifyOutputCode = hipifyOutputCode; }

    public String getFinalRocmCode() { return finalRocmCode; }
    public void setFinalRocmCode(String finalRocmCode) { this.finalRocmCode = finalRocmCode; }

    public int getWavefrontBugsDetected() { return wavefrontBugsDetected; }
    public void setWavefrontBugsDetected(int wavefrontBugsDetected) { this.wavefrontBugsDetected = wavefrontBugsDetected; }

    public Long getEstimatedSavings() { return estimatedSavings; }
    public void setEstimatedSavings(Long estimatedSavings) { this.estimatedSavings = estimatedSavings; }

    public MigrationStatus getStatus() { return status; }
    public void setStatus(MigrationStatus status) { this.status = status; }

    public void setStatusFromString(String statusStr) {
        try {
            this.status = MigrationStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.status = MigrationStatus.FAILED;
        }
    }

    public int getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }

    public boolean isSecurityAuditPassed() { return securityAuditPassed; }
    public void setSecurityAuditPassed(boolean securityAuditPassed) { this.securityAuditPassed = securityAuditPassed; }

    public String getVulnerabilityReport() { return vulnerabilityReport; }
    public void setVulnerabilityReport(String vulnerabilityReport) { this.vulnerabilityReport = vulnerabilityReport; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}