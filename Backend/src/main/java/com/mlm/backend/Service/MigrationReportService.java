package com.mlm.backend.Service;

import com.mlm.backend.Model.MigrationReport;
import com.mlm.backend.Model.Project;
import com.mlm.backend.Repository.MigrationReportRepository;
import com.mlm.backend.Repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MigrationReportService {

    private final MigrationReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final GitHubService gitHubService;

    // Securely pull the token from application.properties
    @Value("${github.api.token:YOUR_FALLBACK_TOKEN}")
    private String githubToken;

    // Default to the organization you created
    @Value("${github.org.name:GemmaForge-Systems}")
    private String githubOrg;

    public MigrationReportService(MigrationReportRepository reportRepository,
                                  ProjectRepository projectRepository,
                                  GitHubService gitHubService) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.gitHubService = gitHubService;
    }

    public MigrationReport createReport(Long projectId, MigrationReport report) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        report.setProject(project);
        return reportRepository.save(report);
    }

    public MigrationReport getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    public List<MigrationReport> getReportsByProject(Long projectId) {
        return reportRepository.findByProjectId(projectId);
    }

    public List<MigrationReport> getPendingReports() {
        return reportRepository.findByStatus(MigrationReport.ReportStatus.PENDING_REVIEW);
    }

    @Transactional
    public MigrationReport reviewReport(Long reportId, MigrationReport.ReportStatus newStatus, String reviewerUsername) {
        MigrationReport report = getReportById(reportId);

        // Prevent re-approving an already approved report to avoid double-counting savings
        if (report.getStatus() == MigrationReport.ReportStatus.APPROVED) {
            throw new RuntimeException("Report is already approved.");
        }

        report.setStatus(newStatus);
        report.setApprovedBy(reviewerUsername);

        // If approved, roll up the savings and execute the DevOps pipeline
        if (newStatus == MigrationReport.ReportStatus.APPROVED) {

            // 1. Roll up the financial savings
            Project project = report.getProject();
            double currentSavings = project.getTotalEstimatedSavings() != null ? project.getTotalEstimatedSavings() : 0.0;
            project.setTotalEstimatedSavings(currentSavings + report.getEstimatedSavings());
            projectRepository.save(project);

            // 2. Execute the Automated GitHub PR Workflow
            try {
                // Ensure the repo name is URL-safe (e.g., "FinTech Kernels" -> "fintech-kernels")
                String repoName = project.getProjectName().replaceAll("\\s+", "-").toLowerCase();
                String branchName = "feature/rocm-migration-" + reportId + "-" + System.currentTimeMillis();
                String prBody = "Automated ROCm Migration securely approved by " + reviewerUsername + " via GemmaForge.";

                // The 3-Step Integration
                gitHubService.createBranch(githubToken, githubOrg, repoName, branchName);
                gitHubService.uploadFileToBranch(githubToken, githubOrg, repoName, report.getOriginalFileName(), report.getRefactoredCode(), branchName, "Refactored CUDA to ROCm");
                gitHubService.createPullRequest(githubToken, githubOrg, repoName, branchName, prBody);

            } catch (Exception e) {
                // In an enterprise app, you'd log this, but we don't want a GitHub failure
                // to rollback the database approval during a demo.
                System.err.println("GitHub Integration Failed: " + e.getMessage());
            }
        }

        return reportRepository.save(report);
    }
}