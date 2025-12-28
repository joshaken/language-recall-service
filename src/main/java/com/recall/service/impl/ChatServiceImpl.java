package com.recall.service.impl;

import com.recall.domain.SentenceDO;
import com.recall.domain.UserAnswerRecordDO;
import com.recall.dto.req.ChatRequest;
import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.ChatResponse;
import com.recall.dto.resp.EvaluationResult;
import com.recall.dto.resp.LlmAccumulator;
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
import reactor.util.function.Tuples;

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
                                Flux<ChatResponse> originalLlmStream = ollamaClient.chat(llmReq)
                                        .publish()
                                        .refCount();
                                // 同时解析 EvaluationResult（用于数据库操作）
                                Mono<EvaluationResult> evalMono = originalLlmStream
                                        .reduce(new LlmAccumulator(), (acc, chunk) -> {
                                            if (chunk.isDone()) {
                                                acc.setFinalMetadata(chunk);
                                            } else {
                                                String text = Optional.ofNullable(chunk.getMessage())
                                                        .map(OllamaMessageDTO::getContent)
                                                        .orElse("");
                                                acc.appendContent(text);
                                            }
                                            return acc;
                                        })
                                        .map(LlmAccumulator::toEvaluationResult)
                                        .cache();


                                return evalMono
                                        .flatMap(eval -> {
                                            Mono<UserAnswerRecordDO> saveMono =
                                                    userAnswerRecordRepoService.saveResult(userId, sentenceId, eval.getCorrect());

                                            Mono<SentenceDO> nextSentenceMono = sentenceRepoService.getNextSentence(sentenceId);

                                            Mono<Void> updateMono = nextSentenceMono
                                                    .flatMap(next -> userRepoService.updateCurrentSentence(userId, next.getId()))
                                                    .then();

                                            return Mono.when(saveMono, updateMono)
                                                    .then(nextSentenceMono.map(next ->
//                                                            {
                                                                    // 构造最终回复文本，并设置回 eval（或用局部变量）
//                                                        String finalReply = eval.getModelAnswer() + "\n\n下一句：" + next.getContent();
//                                                        eval.setModelAnswer(finalReply); // 假设 EvaluationResult 有这个字段
//                                                        return eval;
//                                                    }
                                                                    Tuples.of(eval, eval.getModelAnswer() + "\n\n下一句：" + next.getContent())
                                                    ));
                                        })
                                        .flatMapMany(tuple -> {
                                            EvaluationResult eval = tuple.getT1();
                                            String finalReply = tuple.getT2();

                                            Flux<ChatResponse> replyStream = toTokenStream(finalReply, chatReq.getModel());
                                            return replyStream.concatWithValues(eval.getFinalMetadata());
                                        });
                            });

                })
                .switchIfEmpty(
                        sentenceRepoService.initUserFirstSentence(userId)
                                .flatMapMany(m -> toTokenStream("下一句：" + m, chatReq.getModel()))
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
                                             {"correct": ture | false}
                                        2. 如果不正确，给出不正确的原因
                                        3. 正确的回答和对应的解释，包括
                                            1.自然口语
                                            2.敬语类型的口语
                                                        """)
                                .build(),

                        OllamaMessageDTO.builder()
                                .role("user")
                                .content("""
                                        【中文原句】%s
                                        【用户翻译】%s
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
