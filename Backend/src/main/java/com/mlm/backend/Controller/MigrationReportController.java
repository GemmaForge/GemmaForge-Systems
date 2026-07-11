package com.mlm.backend.Controller;

import com.mlm.backend.Model.MigrationReport;
import com.mlm.backend.Service.MigrationReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class MigrationReportController {

    private final MigrationReportService reportService;

    public MigrationReportController(MigrationReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<MigrationReport> createReport(@PathVariable Long projectId, @RequestBody MigrationReport report) {
        return ResponseEntity.ok(reportService.createReport(projectId, report));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MigrationReport> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MigrationReport>> getReportsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(reportService.getReportsByProject(projectId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MigrationReport>> getPendingReports() {
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<MigrationReport> reviewReport(
            @PathVariable Long id,
            @RequestParam MigrationReport.ReportStatus status,
            @RequestParam String reviewerUsername) {
        MigrationReport updatedReport = reportService.reviewReport(id, status, reviewerUsername);
        return ResponseEntity.ok(updatedReport);
    }
}