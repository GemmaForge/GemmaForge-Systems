package com.mlm.backend.Controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTestController {

    private final ChatClient cloudClient;
    private final ChatClient localClient;

    public AiTestController(
            @Qualifier("cloudClient") ChatClient cloudClient,
            @Qualifier("localClient") ChatClient localClient) {
        this.cloudClient = cloudClient;
        this.localClient = localClient;
    }

    @GetMapping("/test-cloud")
    public String testCloud(@RequestParam String message) {
        return "CLOUD (AMD Hosted Gemma): " + cloudClient.prompt().user(message).call().content();
    }

    @GetMapping("/test-local")
    public String testLocal(@RequestParam String message) {
        return "LOCAL (Ollama/Edge): " + localClient.prompt().user(message).call().content();
    }

    @GetMapping("/test-debug")
    public String debugCloudModel(@RequestParam String message) {
        ChatResponse response = cloudClient.prompt()
                .user(message)
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        String usedModel = response.getMetadata().getModel();

        return "Response: " + content + "\n\nACTUAL MODEL USED: " + usedModel;
    }
}