package com.mlm.backend.Controller;

import com.mlm.backend.Model.MigrationError;
import com.mlm.backend.Service.ROCmAdvisorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advisor")
@CrossOrigin(origins = "*")
public class ROCmAdvisorController {

    private final ROCmAdvisorService advisorService;

    public ROCmAdvisorController(ROCmAdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    // Endpoint to populate the Knowledge Base (Used by Admins)
    @PostMapping("/knowledge-base")
    public ResponseEntity<MigrationError> addErrorEntry(@RequestBody MigrationError error) {
        return ResponseEntity.ok(advisorService.createErrorEntry(error));
    }

    // Endpoint to retrieve all known errors
    @GetMapping("/knowledge-base")
    public ResponseEntity<List<MigrationError>> getAllKnownErrors() {
        return ResponseEntity.ok(advisorService.getAllErrors());
    }

    // Endpoint for the UI to submit a raw compiler log and get advice
    @PostMapping("/analyze")
    public ResponseEntity<MigrationError> analyzeError(@RequestBody Map<String, String> payload) {
        String rawCompilerLog = payload.get("compilerLog");
        if (rawCompilerLog == null || rawCompilerLog.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        MigrationError advice = advisorService.analyzeCompilerOutput(rawCompilerLog);
        return ResponseEntity.ok(advice);
    }

    @DeleteMapping("/knowledge-base/{id}")
    public ResponseEntity<Void> deleteErrorEntry(@PathVariable Long id) {
        advisorService.deleteErrorEntry(id);
        return ResponseEntity.noContent().build();
    }
}