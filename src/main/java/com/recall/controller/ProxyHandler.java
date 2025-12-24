package com.recall.controller;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.ChatResponse;
import com.recall.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//@RestController
//@RequestMapping("/api")
@Slf4j
@Component
public class ProxyHandler {


    private static final String OLLAMA_BASE_URL = "http://localhost:11434/api";


//    @PostMapping(value = "/chat",
//            produces = MediaType.TEXT_EVENT_STREAM_VALUE
//    )
    public Mono<ServerResponse> chat(ServerRequest request) {
        log.info(JsonUtil.toJson(request));
        // 1. 判断语言
        // 2. 查 DB
        // 3. 调 Ollama
        // 4. 组装响应
//        return Mono.justOrEmpty("XXXX");
        ChatResponse chatResponse = new ChatResponse();
        Flux<ChatResponse> just = Flux.just(chatResponse);
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(just, ChatResponse.class);
    }

}
