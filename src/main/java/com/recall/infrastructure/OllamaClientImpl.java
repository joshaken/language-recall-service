package com.recall.infrastructure;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.resp.ChatResponse;
import com.recall.infrastructure.repository.OllamaClient;
import com.recall.utils.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class OllamaClientImpl implements OllamaClient {

    @Resource
    private WebClient webClient;

    @Override
    public Flux<ChatResponse> chat(ChatRequest request) {
        return webClient.post()
                .uri("/api/chat")
//                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .exchangeToFlux(resp -> {
                    log.info("Ollama req = {}", JsonUtil.toJson(request));
                    log.info("Ollama status = {}", resp.statusCode());
                    log.info("Ollama content-type = {}", resp.headers().contentType());

                    return resp.bodyToFlux(String.class)
                            .doOnNext(body ->
                                    log.info("Ollama raw chunk >>> {}", body)
                            ).mapNotNull(x -> JsonUtil.toObject(x, ChatResponse.class));
                })
//                .retrieve()
//                .bodyToFlux(EvaluationResult.class)
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
                ;
    }
}
