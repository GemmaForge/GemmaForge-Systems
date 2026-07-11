package com.mlm.backend.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DiscordWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordWebhookService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${discord.webhook.url:}")
    private String discordWebhookUrl;

    public void sendMigrationAlert(String projectName, String fileName, String status, String summary, boolean passed) {
        if (discordWebhookUrl == null || discordWebhookUrl.isBlank()) {
            logger.warn("Discord Alert requested but webhook URL is unconfigured.");
            return;
        }

        try {
            // Determine embed color side (Green for success, Crimson for security block/failure)
            int embedColor = passed ? 3066993 : 15158332;

            // Building the structured Discord embed payload mapping
            Map<String, Object> embed = Map.of(
                    "title", "🛡️ GemmaForge Audit Dispatch",
                    "description", "**Project:** " + projectName + "\n**Source Kernel:** `" + fileName + "`",
                    "color", embedColor,
                    "fields", List.of(
                            Map.of("name", "Status Pipeline", "value", "`" + status + "`", "inline", true),
                            Map.of("name", "Security Clearance", "value", passed ? "✅ PASSED" : "⚠️ VULNERABILITY DETECTED", "inline", true),
                            Map.of("name", "Analysis Diagnostics", "value", summary.length() > 500 ? summary.substring(0, 500) + "..." : summary, "inline", false)
                    ),
                    "footer", Map.of("text", "Heterogeneous Core Analytics Engine")
            );

            Map<String, Object> body = Map.of(
                    "username", "GemmaForge Advisor Bot",
                    "embeds", List.of(embed)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForLocation(discordWebhookUrl, entity);

            logger.info("DevOps Discord channel notification delivered successfully for project: {}", projectName);
        } catch (Exception e) {
            logger.error("Failed to fire external Discord webhook event alert hook: {}", e.getMessage());
        }
    }
}