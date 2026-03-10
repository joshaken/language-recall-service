package com.recall.infrastructure.ai;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.OllamaChatResponse;
import reactor.core.publisher.Flux;

public interface IAiService {
    Flux<OllamaChatResponse> chat(String sentence, String userInput, ChatRequest chatReq);
}
