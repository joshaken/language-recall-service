package com.recall.service;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.OllamaChatResponse;
import reactor.core.publisher.Flux;

/**
 * Service interface for chat operations.
 */
public interface IChatService {
    /**
     * Performs a chat operation.
     * @param chatReq The chat request
     * @return A stream of chat responses
     */
    Flux<OllamaChatResponse> chat(ChatRequest chatReq);
}
