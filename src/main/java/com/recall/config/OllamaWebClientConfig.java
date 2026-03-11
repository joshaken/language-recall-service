package com.recall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the Ollama WebClient.
 */
@Configuration
public class OllamaWebClientConfig {

    /**
     * Creates a WebClient bean configured to communicate with the Ollama service.
     * @param builder The WebClient builder
     * @return The configured WebClient instance
     */
    @Bean
    public WebClient ollamaWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:11434")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
