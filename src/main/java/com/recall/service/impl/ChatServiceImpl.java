package com.recall.service.impl;

import com.recall.domain.SentenceDO;
import com.recall.domain.UserAnswerRecordDO;
import com.recall.dto.req.ChatRequest;
import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.ChatResponse;
import com.recall.dto.resp.EvaluationResult;
import com.recall.infrastructure.repository.OllamaClient;
import com.recall.infrastructure.repository.SentenceRepoService;
import com.recall.infrastructure.repository.UserAnswerRecordRepoService;
import com.recall.infrastructure.repository.UserRepoService;
import com.recall.service.IChatService;
import com.recall.utils.CustomStringUtil;
import com.recall.utils.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {
//    private static final String OLLAMA_BASE_URL = "http://localhost:11434/api";

    @Resource
    private UserRepoService userRepoService;

    @Resource
    private SentenceRepoService sentenceRepoService;

    @Resource
    private UserAnswerRecordRepoService userAnswerRecordRepoService;

    @Resource
    private OllamaClient ollamaClient;

    @Override
    public Flux<ChatResponse> chat(ChatRequest chatReq) {
        OllamaMessageDTO lastMessage = chatReq.getMessages()
                .stream()
                .filter(x -> "user".equals(x.getRole()))
                .reduce((a, b) -> b) // 取最后一条
                .orElseThrow(() -> new IllegalArgumentException("No user message"));

        String userInput = lastMessage.getContent();
        Long userId = 1L;

        return userRepoService.findUserCurrentSentence(userId)
                .flatMapMany(sentenceId -> {
                    log.info("user sentenceId {}", sentenceId);
                    return sentenceRepoService.loadSentence(sentenceId)
                            .flatMapMany(sentence -> {
                                if (!CustomStringUtil.containsJapanese(userInput)) {
                                    // 中文：直接返回“下一句：xxx” 的 token 流
                                    String content = "下一句：" + sentence;
                                    return toTokenStream(content, chatReq.getModel());
                                }

                                // 非中文：走 Ollama 评估流程
                                ChatRequest llmReq = buildEvaluateRequest(sentence, userInput, chatReq);
                                Flux<ChatResponse> originalLlmStream = ollamaClient.chat(llmReq);
                                // 同时解析 EvaluationResult（用于数据库操作）
                                Mono<EvaluationResult> evalMono = originalLlmStream
                                        .takeUntil(ChatResponse::isDone)
                                        .map(chunk -> Optional.ofNullable(chunk.getMessage())
                                                .map(OllamaMessageDTO::getContent).orElse(""))
                                        .filter(StringUtils::hasText)
                                        .reduce(new StringBuilder(), StringBuilder::append)
                                        .map(StringBuilder::toString)
                                        .map(fullText -> {
                                                    // Step 1: 清洗可能的 Markdown 包裹
                                                    String cleaned = fullText.trim();
                                                    if (cleaned.startsWith("```json")) {
                                                        cleaned = cleaned.substring(7); // remove ```json
                                                    }
                                                    if (cleaned.startsWith("```")) {
                                                        cleaned = cleaned.substring(3);
                                                    }
                                                    if (cleaned.endsWith("```")) {
                                                        cleaned = cleaned.substring(0, cleaned.length() - 3);
                                                    }
                                                    cleaned = cleaned.trim();

                                                    // Step 2: 尝试解析 JSON
                                                    try {
                                                        return JsonUtil.toObject(cleaned, EvaluationResult.class);
                                                    } catch (Exception e) {
                                                        log.warn("Failed to parse LLM response as JSON, trying regex fallback. Raw: {}", cleaned, e);
                                                    }

                                                    // Step 3: 正则/关键词提取 fallback
                                                    boolean correct = cleaned.toLowerCase().contains("\"correct\": \"true\"") ||
                                                            cleaned.toLowerCase().contains("'correct': 'true'") ||
                                                            cleaned.toLowerCase().contains("correct: true");
                                                    return EvaluationResult.builder()
                                                            .correct(correct)
                                                            .build();
                                                }
                                        )
                                        .cache(); // cache 避免重复订阅

                                Mono<Void> sideEffects = evalMono
                                        .flatMap(eval -> {
                                            Mono<UserAnswerRecordDO> saveMono = userAnswerRecordRepoService
                                                    .saveResult(userId, sentenceId, eval.getCorrect());

                                            Mono<SentenceDO> nextSentenceMono = sentenceRepoService.getNextSentence(sentenceId);

                                            Mono<Void> updateMono = nextSentenceMono
                                                    .flatMap(next -> userRepoService.updateCurrentSentence(userId, next.getId()))
                                                    .then();

                                            return Mono.when(saveMono, updateMono).then();
                                        })
                                        .then();

                                Mono<String> finalAppendContent = sentenceRepoService.getNextSentence(sentenceId)
                                        .map(SentenceDO::getContent);

                                return originalLlmStream
                                        .filter(response -> !response.isDone()) // 直接过滤掉 done=true 的 chunk
                                        .concatWith(
                                                sideEffects.thenMany(
                                                        finalAppendContent.flatMapMany(content ->
                                                                toTokenStream(chatReq.getModel(), content, false)
                                                        )
                                                )
                                        )
                                        .concatWithValues(createDoneResponse(chatReq.getModel()));
                            });
                })
                .switchIfEmpty(
                        sentenceRepoService.initUserFirstSentence(userId).flatMapMany(m -> {
                            return toTokenStream(m, chatReq.getModel());
                        })
                );
    }


    private ChatRequest buildEvaluateRequest(String sentence, String userInput, ChatRequest chatReq) {
        return ChatRequest.builder()
                .model(chatReq.getModel())
                .messages(List.of(
                        OllamaMessageDTO.builder()
                                .role("system")
                                .content("""
                                        你是一个日语母语者。
                                        你的任务是评估外国人对给定中文句子的日语翻译。

                                        请从以下方面进行评价：
                                        1. 语法是否正确
                                        2. 助词使用是否恰当
                                        3. 表达是否自然、符合语境
                                                                                
                                        需要响应：
                                        1. 用户翻译是否正确的json
                                             {"correct": "ture | false"},
                                        2. 如果不正确，给出不正确的原因
                                        3. 给出正确的回答和对应的解释，包括
                                            3.1 自然口语
                                            3.2 敬语类型的口语
                                                                            
                                                        """)
                                .build(),

                        OllamaMessageDTO.builder()
                                .role("user")
                                .content("""
                                        【中文原句】
                                        %s
                                        【用户翻译】
                                        %s
                                                        """.formatted(sentence, userInput))
                                .build()
                ))
                .stream(true)
                .build();
    }

    private Flux<ChatResponse> toTokenStream(String fullContent, String model) {
        List<String> tokens = CustomStringUtil.splitByLengthStream(fullContent, 5);

        Flux<ChatResponse> tokenFlux = Flux.fromIterable(tokens)
                .delayElements(Duration.ofMillis(1))
                .map(token -> ChatResponse.builder()
                        .message(OllamaMessageDTO.builder().content(token).build())
                        .model(model)
                        .created_at(Instant.now())
                        .done(false)
                        .build()
                );

        ChatResponse doneFrame = ChatResponse.builder()
                .message(OllamaMessageDTO.builder().content("").build())
                .model(model)
                .created_at(Instant.now())
                .done(true)
                .done_reason("stop")
                .eval_count((long) tokens.size())
                .build();

        return tokenFlux.concatWithValues(doneFrame);
    }

    // 生成 token 流，但每个 chunk 的 done = false（由外部控制结束）
    private Flux<ChatResponse> toTokenStream(String model, String content, boolean includeDone) {
        if (content == null || content.isEmpty()) {
            if (includeDone) {
                return Flux.just(createDoneResponse(model));
            } else {
                return Flux.empty();
            }
        }

        List<String> tokens = CustomStringUtil.splitByLengthStream(content, 5);

        Flux<ChatResponse> stream = Flux.fromIterable(tokens)
                .map(token -> ChatResponse.builder()
                        .model(model)
                        .message(OllamaMessageDTO.builder()
                                .role("assistant")
                                .content(token)
                                .build())
                        .created_at(Instant.now())
                        .done(false) // ← 关键：不是 done
                        .build());

        if (includeDone) {
            return stream.concatWithValues(createDoneResponse(model));
        } else {
            return stream;
        }
    }

    private ChatResponse createDoneResponse(String model) {
        return ChatResponse.builder()
                .model(model)
                .message(OllamaMessageDTO.builder()
                        .role("assistant")
                        .content("")
                        .build())
                .created_at(Instant.now())
                .done(true)
                .done_reason("stop")
                .build();
    }

}
