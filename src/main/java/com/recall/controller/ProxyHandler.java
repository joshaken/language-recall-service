package com.recall.controller;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.OllamaChatResponse;
import com.recall.service.IChatService;
import com.recall.utils.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
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
        return request.bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Request body is missing")))
                .flatMap(x -> {
                    log.info("Received chat request: {}", JsonUtil.toJson(x));
                    ChatRequest chatReq = JsonUtil.toObject(x, ChatRequest.class);
                    Flux<OllamaChatResponse> responseStream = iChatService.chat(chatReq);

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_NDJSON)
//                            .contentType(MediaType.TEXT_EVENT_STREAM)
                            .body(responseStream
                                    , OllamaChatResponse.class);
                })
                .onErrorResume(ex -> {
                    log.error("Error in chat handler", ex);
                    return ServerResponse.badRequest()
                            .bodyValue("Error: " + ex.getMessage());
                });
    }



}
