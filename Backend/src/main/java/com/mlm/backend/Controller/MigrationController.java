package com.mlm.backend.Controller;

import com.mlm.backend.Model.Migration;
import com.mlm.backend.Service.MigrationEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private static final Logger logger = LoggerFactory.getLogger(MigrationController.class);
    private final MigrationEngineService migrationEngineService;

    public MigrationController(MigrationEngineService migrationEngineService) {
        this.migrationEngineService = migrationEngineService;
    }

    // 1. Diagnostic Endpoint
    @PostMapping("/diagnose")
    public ResponseEntity<Map<String, String>> diagnoseCompilerError(@RequestBody String rawLogs) {
        if (rawLogs == null || rawLogs.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Logs cannot be empty."));
        }
        String explanation = migrationEngineService.getCompilerDiagnosis(rawLogs);
        return ResponseEntity.ok(Map.of("explanation", explanation));
    }

    // 2. Analytics Endpoints
    @GetMapping("/analytics/projects-count")
    public ResponseEntity<Long> getProjectsCount() {
        return ResponseEntity.ok(migrationEngineService.getCompletedProjectsCount());
    }

    @GetMapping("/analytics/total-savings")
    public ResponseEntity<Long> getTotalSavings() {
        return ResponseEntity.ok(migrationEngineService.getTotalCorporateSavings());
    }

    // --- NEW: Project History Endpoint (Module 1) ---
    @GetMapping("/history")
    public ResponseEntity<List<Migration>> getProjectHistory(@RequestParam("projectName") String projectName) {
        if (projectName == null || projectName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(migrationEngineService.getProjectHistory(projectName));
    }

    // 3. Governance: Approval
    @PostMapping("/approve/{id}")
    public ResponseEntity<Void> approveMigration(@PathVariable Long id) {
        migrationEngineService.approveMigration(id);
        return ResponseEntity.ok().build();
    }

    // 4. MASTERY WORKFLOW: Processes migration with TIER support
    @PostMapping("/process")
    public ResponseEntity<?> startMigration(
            @RequestParam("projectName") String projectName,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "tier", defaultValue = "EXPERT") String tier,
            @RequestBody(required = false) String cudaCode) {

        logger.info("Mastery Workflow Request | Project: {} | Tier: {} | File: {}", projectName, tier, fileName);

        if (projectName == null || projectName.trim().isEmpty() ||
                fileName == null || fileName.trim().isEmpty() ||
                cudaCode == null || cudaCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters."));
        }

        try {
            // Now correctly passing the 4th argument 'tier' to the Service
            Migration completedMigration = migrationEngineService.processMigration(projectName, fileName, cudaCode, tier);
            logger.info("Migration workflow completed successfully for project: {}", projectName);
            return ResponseEntity.ok(completedMigration);
        } catch (Exception e) {
            logger.error("Migration Workflow Exception: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Migration Workflow Failed", "details", e.getMessage()));
        }
    }
}