package com.mlm.backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class GitHubService {
    private final RestTemplate restTemplate = new RestTemplate();

    // Step 1: Create a new branch from main
    public void createBranch(String token, String owner, String repo, String newBranchName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Get the SHA of the main branch
        String mainRefUrl = String.format("https://api.github.com/repos/%s/%s/git/refs/heads/main", owner, repo);
        ResponseEntity<Map> response = restTemplate.exchange(mainRefUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> objectNode = (Map<String, Object>) response.getBody().get("object");
        String sha = objectNode.get("sha").toString();

        // Create the new branch pointing to that SHA using a Map for safe JSON parsing
        String createRefUrl = String.format("https://api.github.com/repos/%s/%s/git/refs", owner, repo);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("ref", "refs/heads/" + newBranchName);
        requestBody.put("sha", sha);

        restTemplate.postForObject(createRefUrl, new HttpEntity<>(requestBody, headers), String.class);
    }

    // Step 2: Upload the refactored code to the new branch
    public void uploadFileToBranch(String token, String owner, String repo, String path, String content, String branch, String message) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
        String encodedContent = java.util.Base64.getEncoder().encodeToString(content.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Use Map to safely serialize JSON
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("message", message);
        requestBody.put("content", encodedContent);
        requestBody.put("branch", branch);

        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody, headers), String.class);
    }

    // Step 3: Create the Pull Request
    public void createPullRequest(String token, String owner, String repo, String branch, String body) {
        String url = String.format("https://api.github.com/repos/%s/%s/pulls", owner, repo);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Use Map to safely escape all AI-generated newlines and quotes
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "GemmaForge: ROCm Migration");
        requestBody.put("head", branch);
        requestBody.put("base", "main");
        requestBody.put("body", body);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        restTemplate.postForObject(url, entity, String.class);
    }
}