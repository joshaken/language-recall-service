package com.recall.infrastructure.ai.impl;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.OllamaChatResponse;
import com.recall.infrastructure.ai.OllamaClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(value = "config.ai.method", havingValue = "spring", matchIfMissing = true)
@Service
@Slf4j
public class OllamaAiSpringAiClientImpl implements OllamaClient {

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    private final PromptTemplate userPrompt;
    private final PromptTemplate sysPrompt;

    @Resource
    private OllamaApi ollamaApi;

    public OllamaAiSpringAiClientImpl(
            @Value("classpath:prompts/input-evaluation-user.st") org.springframework.core.io.Resource userPromptResource
            , @Value("classpath:prompts/input-evaluation-sys.st") org.springframework.core.io.Resource sysPromptResource
    ) throws IOException {
        this.sysPrompt = new PromptTemplate(sysPromptResource.getContentAsString(StandardCharsets.UTF_8));
        this.userPrompt = new PromptTemplate(userPromptResource.getContentAsString(StandardCharsets.UTF_8));
    }

    @Override
    public Flux<OllamaChatResponse> chat(String sentence, String userInput, ChatRequest chatReq) {
        String renderedUserPrompt = userPrompt.render(Map.of(
                "sentence", sentence,
                "userInput", userInput
        ));
        OllamaApi.Message sysMessage = OllamaApi.Message.builder(OllamaApi.Message.Role.SYSTEM)
                .content(sysPrompt.getTemplate())
                .build();
        OllamaApi.Message userMessage = OllamaApi.Message.builder(OllamaApi.Message.Role.USER)
                .content(renderedUserPrompt)
                .build();
        OllamaApi.ChatRequest request = OllamaApi.ChatRequest.builder(model)
                .stream(true)
                .messages(List.of(
                        sysMessage,
                        userMessage
                ))
                .build();

        return ollamaApi.streamingChat(request)
                .filter(resp -> resp.message() != null)
                .map(resp -> OllamaChatResponse.builder()
                        .model(resp.model())
                        .createdAt(resp.createdAt())
                        .message(OllamaMessageDTO.builder()
                                .role(resp.message().role().name())
                                .content(resp.message().content())
                                .build())
                        .done(resp.done())
                        .doneReason(resp.doneReason())
                        .totalDuration(resp.totalDuration())
                        .loadDuration(resp.loadDuration())
                        .promptEvalCount(resp.promptEvalCount())
                        .promptEvalDuration(resp.promptEvalDuration())
                        .evalCount(resp.evalCount())
                        .evalDuration(resp.evalDuration())
                        .build()
                );

    }


}
