package com.recall.infrastructure.ai.impl;

import com.recall.dto.req.ChatRequest;
import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.OllamaChatResponse;
import com.recall.infrastructure.ai.IAiService;
import com.recall.utils.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@ConditionalOnProperty(value = "config.ai.method", havingValue = "http")
@Service
@Slf4j
public class OllamaAiWebClientImpl implements IAiService {

    @Resource
    private WebClient webClient;

    @Override
    public Flux<OllamaChatResponse> chat(String sentence, String userInput, ChatRequest chatReq) {
        ChatRequest request = buildEvaluateRequest(sentence, userInput, chatReq);
        return webClient.post()
                .uri("/api/chat")
//                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .exchangeToFlux(resp -> {
                    log.info("Ollama req = {}", JsonUtil.toJson(request));
                    log.info("Ollama status = {}", resp.statusCode());
                    log.info("Ollama content-type = {}", resp.headers().contentType());

                    return resp.bodyToFlux(String.class)
//                            .doOnNext(body ->
//                                    log.info("Ollama raw chunk >>> {}", body)
//                            )
                            .mapNotNull(x -> JsonUtil.toObject(x, OllamaChatResponse.class));
                })
//                .retrieve()
//                .bodyToFlux(EvaluationResult.class)
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
                ;
    }

    private ChatRequest buildEvaluateRequest(String sentence, String userInput, ChatRequest chatReq) {
        return ChatRequest.builder()
                .model(chatReq.getModel())
                .messages(List.of(
                        OllamaMessageDTO.builder()
                                .role("system")
                                .content(getSystemContent())
                                .build(),

                        OllamaMessageDTO.builder()
                                .role("user")
                                .content(getUserContent(sentence, userInput))
                                .build()
                ))
                .stream(true)
                .build();
    }

    private static String getUserContent(String sentence, String userInput) {
        return """
                【中文原句】%s【用户翻译】%s
                                """.formatted(sentence, userInput);
    }

    private static String getSystemContent() {
        return """
                你是一个日语母语者。
                你的任务是评估外国人对给定中文句子的日语翻译。

                请从以下方面进行评价：
                1. 语法是否正确
                2. 助词使用是否恰当
                3. 表达是否自然、符合语境
                                                        
                需要响应：
                1. 用户翻译是否正确的json
                     {"correct": ture | false}
                2. 如果不正确，给出不正确的原因
                3. 正确的回答和对应的解释，包括
                    1.自然口语
                    2.敬语类型的口语
                                """;
    }
}
