package com.mlm.backend.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // Primary Client -> Routes to your high-performance Droplet running Gemma 4 12B via OpenAI protocol
    @Bean("cloudClient")
    public ChatClient cloudClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    // Fallback Client -> Routes to your local edge Ollama instance
    @Bean("localClient")
    public ChatClient localClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }
}