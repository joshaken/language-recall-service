package com.recall.infrastructure.repository;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.OllamaChatResponse;
import reactor.core.publisher.Flux;

public interface OllamaClient {
    Flux<OllamaChatResponse> chat(String sentence, String userInput, ChatRequest chatReq);

    Flux<OllamaChatResponse> chat2(String sentence, String userInput, ChatRequest chatReq);

}
