package com.recall.service;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.OllamaChatResponse;
import reactor.core.publisher.Flux;

public interface IChatService {
    Flux<OllamaChatResponse> chat(ChatRequest chatReq);
}
