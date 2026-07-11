package com.mlm.backend.Service;

import com.mlm.backend.Model.Migration;
import com.mlm.backend.Model.Migration.MigrationStatus;
import com.mlm.backend.Repository.MigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MigrationEngineService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationEngineService.class);
    private final MigrationRepository migrationRepository;

    private final ChatClient localClient;
    private final ChatClient cloudClient;

    private final DiscordWebhookService discordWebhookService;
    private final GitHubService gitHubService;

    @Value("${github.token:}")
    private String githubToken;

    @Value("${github.owner:}")
    private String githubOwner;

    @Value("${github.repo:}")
    private String githubRepo;

    public MigrationEngineService(MigrationRepository migrationRepository,
                                  @Qualifier("localClient") ChatClient localClient,
                                  @Qualifier("cloudClient") ChatClient cloudClient,
                                  DiscordWebhookService discordWebhookService,
                                  GitHubService gitHubService) {
        this.migrationRepository = migrationRepository;
        this.localClient = localClient;
        this.cloudClient = cloudClient;
        this.discordWebhookService = discordWebhookService;
        this.gitHubService = gitHubService;
    }

    @Transactional
    public Migration processMigration(String projectName, String fileName, String cudaCode, String tier) {
        logger.info("GemmaForge Hybrid Pipeline Activated | Tier: {} | Project: {}", tier, projectName);

        Migration migration = new Migration();
        migration.setProjectName(projectName);
        migration.setOriginalFileName(fileName);
        migration.setOriginalCudaCode(cudaCode);
        migration.setStatus(MigrationStatus.PROCESSING);
        migration = migrationRepository.save(migration);

        long startTime = System.currentTimeMillis();

        try {
            // 1. Core Native Translation Pass
            String hipified = runNativeHipify(cudaCode);
            migration.setHipifyOutputCode(hipified);

            // 2. Cognitive Cloud Refactoring Pass
            String aiResponse = runMasteryRefinement(hipified, tier);

            // Resilient token split parsing using regex markers
            String optimizedCode = extractCodeSegment(aiResponse);
            String explanation = extractExplanationSegment(aiResponse, optimizedCode);

            migration.setFinalRocmCode(optimizedCode);

            // 3. Security & Wavefront Audit on local vLLM Node
            String auditResult = performMasteryAudit(optimizedCode, tier);

            // Clean up wrapping markdown to match validation conditions robustly
            String cleanAudit = auditResult.replaceAll("\\*", "").trim().toUpperCase();
            boolean isPassed = cleanAudit.startsWith("PASSED") || cleanAudit.contains("PASSED");

            migration.setSecurityAuditPassed(isPassed);
            migration.setVulnerabilityReport(
                    (isPassed ? "GemmaForge Mastery Audit: Wavefront alignment and memory layout verified safe." : auditResult)
                            + "\n\n--- Technical Structural Breakdown ---\n" + explanation
            );

            // 4. Wavefront Telemetry Calculations
            int fixes = countOccurrences(optimizedCode, "/* Wavefront adjustment */");
            migration.setWavefrontBugsDetected(fixes);
            migration.setEstimatedSavings(12500L + (fixes * 25000L));

            long endTime = System.currentTimeMillis();
            migration.setExecutionTimeMs(endTime - startTime);
            migration.setStatus(MigrationStatus.PENDING);

        } catch (Exception e) {
            migration.setStatus(MigrationStatus.FAILED);
            migration.setVulnerabilityReport("Pipeline Execution Fault: " + e.getMessage());
            logger.error("Error executing core hybrid model pipeline: ", e);
        }

        // Monitoring Push
        this.discordWebhookService.sendMigrationAlert(
                migration.getProjectName(),
                migration.getOriginalFileName(),
                migration.getStatus().name(),
                migration.getVulnerabilityReport(),
                migration.isSecurityAuditPassed()
        );

        return migrationRepository.save(migration);
    }

    public List<Migration> getProjectHistory(String projectName) {
        return migrationRepository.findByProjectNameOrderByCreatedAtDesc(projectName);
    }

    private String runMasteryRefinement(String code, String tier) {
        String instructions = switch (tier) {
            case "MASTER" -> "ROLE: GPU Master. Task: Advanced vectorization, explicit memory alignment for 64-thread wavefronts, and loop unrolling.";
            case "EXPERT" -> "ROLE: GPU Expert. Task: Map 32-lane logic to 64-lane wavefronts, fix standard architectural traps.";
            default -> "ROLE: GPU Senior. Task: Translate syntax, fix basic mapping. Keep it simple.";
        };

        String prompt = "Task: Convert CUDA to ROCm.\n" +
                "STRICT FORMAT PARADIGM: Start your response with token identifier [CODE] followed by raw source content. Then output [EXPLANATION] followed by your architectural notes.\n" +
                "Code:\n" + code;

        return cloudClient.prompt()
                .system(instructions)
                .user(prompt)
                .call()
                .content();
    }

    private String performMasteryAudit(String code, String tier) {
        String auditPrompt = switch (tier) {
            case "MASTER" -> "Perform exhaustive security audit, bank-grade memory safety check, and performance bottleneck analysis.";
            case "EXPERT" -> "Check for buffer overflows, pointer safety, and wavefront alignment bugs.";
            default -> "Check for basic syntax errors and critical memory safety.";
        };

        return localClient.prompt()
                .system("Security Researcher. If secure, output ONLY 'PASSED'. If not, list structural optimization issues explicitly.")
                .user(auditPrompt + "\nCode:\n" + code)
                .call()
                .content();
    }

    private String generateTechnicalExplanation(String code) {
        return localClient.prompt()
                .system("Role: Tech Lead. Provide a concise bullet-point technical explanation of memory alignment, loop unrolling, and vectorization used in this code.")
                .user(code)
                .call()
                .content();
    }

    private String runNativeHipify(String cudaCode) {
        try {
            ProcessBuilder pb = new ProcessBuilder("hipify-perl", "--inline");
            Process p = pb.start();

            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
                w.write(cudaCode);
                w.flush();
            }

            // Prevent hanging if compilation process deadlocks
            boolean completed = p.waitFor(10, TimeUnit.SECONDS);
            if (!completed) {
                p.destroyForcibly();
                logger.warn("Native hipify process timed out. Falling back to clean regex mapping.");
                return fallbackSyntaxMapping(cudaCode);
            }

            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) b.append(line).append("\n");
                return b.toString().isBlank() ? fallbackSyntaxMapping(cudaCode) : b.toString();
            }
        } catch (Exception e) {
            return fallbackSyntaxMapping(cudaCode);
        }
    }

    private String fallbackSyntaxMapping(String source) {
        return source.replace("cudaMemcpy", "hipMemcpy")
                .replace("cudaMalloc", "hipMalloc")
                .replace("cudaFree", "hipFree")
                .replace("__global__", "__global__")
                .replace("cudaSuccess", "hipSuccess");
    }

    private String extractCodeSegment(String raw) {
        if (!raw.contains("[CODE]")) {
            return raw.split("\\[EXPLANATION\\]")[0].replaceAll("```(cpp|cl|cu)?", "").replaceAll("```", "").trim();
        }
        Pattern pattern = Pattern.compile("\\[CODE\\]([\\s\\S]*?)(?=\\[EXPLANATION\\]|$)");
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1).replaceAll("```(cpp|cl|cu)?", "").replaceAll("```", "").trim();
        }
        return raw.replaceAll("```(cpp|cl|cu)?", "").replaceAll("```", "").trim();
    }

    private String extractExplanationSegment(String raw, String fallbackCode) {
        if (raw.contains("[EXPLANATION]")) {
            return raw.split("\\[EXPLANATION\\]")[1].trim();
        }
        return generateTechnicalExplanation(fallbackCode);
    }

    @Transactional
    public void approveMigration(Long id) {
        Migration m = migrationRepository.findById(id).orElseThrow(() -> new RuntimeException("Migration task index not found"));

        m.setStatus(MigrationStatus.COMPLETED);
        migrationRepository.save(m);

        try {
            if (githubToken == null || githubToken.isBlank()) {
                logger.warn("GitHub DevOps integration token missing from properties configuration. Skipping remote branch deployment.");
                return;
            }

            String cleanProjectName = m.getProjectName().replaceAll("\\s+", "-").toLowerCase();
            String branchName = "rocm-migration-" + cleanProjectName + "-" + id + "-" + System.currentTimeMillis();
            String newFileName = m.getOriginalFileName().replace(".cu", "").replace(".cpp", "") + "_rocm_" + System.currentTimeMillis() + ".cpp";
            String commitMsg = "Enterprise Refactor: Automated CUDA to ROCm/HIP structural migration";

            String prBody = "### 🤖 GemmaForge Automated Migration\n" +
                    "This Pull Request has been compiled using the GemmaForge Hybrid Orchestration engine.\n\n" +
                    "**Structural Integrity & Local Compliance Verification:**\n" +
                    m.getVulnerabilityReport() + "\n\n" +
                    "**Calculated Modernization Savings:** R " + m.getEstimatedSavings();

            String commentedExplanation = "/* =====================================================================\n" +
                    " * 🤖 GEMMAFORGE AUTOMATED MIGRATION REPORT\n" +
                    " * =====================================================================\n" +
                    " * " + m.getVulnerabilityReport().replace("\n", "\n * ") + "\n" +
                    " * ===================================================================== */\n\n";

            String finalFileContent = commentedExplanation + m.getFinalRocmCode();

            gitHubService.createBranch(githubToken, githubOwner, githubRepo, branchName);
            gitHubService.uploadFileToBranch(githubToken, githubOwner, githubRepo, newFileName, finalFileContent, branchName, commitMsg);
            gitHubService.createPullRequest(githubToken, githubOwner, githubRepo, branchName, prBody);

            logger.info("DevOps Pull Request deployed successfully for project target: {}", m.getProjectName());
        } catch (Exception e) {
            logger.error("GitHub Automation Flow Interrupted: {}", e.getMessage());
        }
    }

    public String getCompilerDiagnosis(String rawLogs) {
        String systemInstruction = "ROLE: AMD ROCm/HIP Compiler Expert. Translate errors into plain English and suggest structural fixes.";
        return localClient.prompt().system(systemInstruction).user("Analyze compilation trace logs: " + rawLogs).call().content();
    }

    public Long getTotalCorporateSavings() { return migrationRepository.calculateTotalCorporateSavings(); }
    public Long getCompletedProjectsCount() { return migrationRepository.countCompletedProjects(); }
    private int countOccurrences(String t, String target) { return (t.length() - t.replace(target, "").length()) / target.length(); }
}