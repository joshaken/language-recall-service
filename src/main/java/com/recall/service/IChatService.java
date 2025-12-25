package com.recall.service;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.ChatResponse;
import reactor.core.publisher.Flux;

public interface IChatService {
    Flux<ChatResponse> chat(ChatRequest chatReq);
}
