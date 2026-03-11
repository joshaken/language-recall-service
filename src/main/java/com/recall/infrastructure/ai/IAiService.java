package com.recall.infrastructure.ai;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.OllamaChatResponse;
import reactor.core.publisher.Flux;

/**
 * Interface for AI service operations.
 */
public interface IAiService {
    /**
     * Performs a chat operation with the AI model.
     * @param sentence The current sentence context
     * @param userInput The user's input
     * @param chatReq The chat request parameters
     * @return A stream of chat responses
     */
    Flux<OllamaChatResponse> chat(String sentence, String userInput, ChatRequest chatReq);
}
