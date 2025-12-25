package com.recall.service.impl;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.ChatResponse;
import com.recall.service.IChatService;
import com.recall.utils.CustomStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {
//    private static final String OLLAMA_BASE_URL = "http://localhost:11434/api";

    @Override
    public Flux<ChatResponse> chat(ChatRequest chatReq) {
        chatReq.getMessages().stream()
                .filter(x ->
                        CustomStringUtil.containsChinese(x.getContent())
                )

        ;
        return null;
    }

    // 1. 判断语言
    // 2. 查 DB
    // 3. 调 Ollama
}
