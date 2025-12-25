package com.recall.controller;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.ChatResponse;
import com.recall.service.IChatService;
import com.recall.utils.JsonUtil;
import jakarta.annotation.Resource;
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

    @Resource
    private IChatService iChatService;
    //    @PostMapping(value = "/chat",
//            produces = MediaType.TEXT_EVENT_STREAM_VALUE
//    )
    public Mono<ServerResponse> chat(ServerRequest request) {
        return request.bodyToMono(ChatRequest.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Request body is missing")))
                .flatMap(chatReq -> {
                    log.info("Received chat request: {}", JsonUtil.toJson(chatReq));

                    Flux<ChatResponse> responseStream = iChatService.chat(chatReq);

                    return ServerResponse.ok()
                            .contentType(MediaType.TEXT_EVENT_STREAM)
                            .body(responseStream, ChatResponse.class);
                })
                .onErrorResume(ex -> {
                    log.error("Error in chat handler", ex);
                    return ServerResponse.badRequest()
                            .bodyValue("Error: " + ex.getMessage());
                });
    }

}
