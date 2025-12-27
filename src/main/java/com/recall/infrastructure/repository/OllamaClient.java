package com.recall.infrastructure.repository;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.ChatResponse;
import reactor.core.publisher.Flux;

public interface OllamaClient {
    Flux<ChatResponse> chat(ChatRequest request);

}
