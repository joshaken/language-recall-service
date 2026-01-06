package com.recall.service.impl;

import com.recall.domain.SentenceDO;
import com.recall.domain.UserAnswerRecordDO;
import com.recall.dto.req.ChatRequest;
import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.OllamaChatResponse;
import com.recall.dto.resp.EvaluationResult;
import com.recall.dto.resp.LlmAccumulator;
import com.recall.infrastructure.repository.OllamaClient;
import com.recall.infrastructure.repository.SentenceRepoService;
import com.recall.infrastructure.repository.UserAnswerRecordRepoService;
import com.recall.infrastructure.repository.UserRepoService;
import com.recall.service.IChatService;
import com.recall.utils.CustomStringUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    public Flux<OllamaChatResponse> chat(ChatRequest chatReq) {
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
                                    return toTokenStream(content, chatReq.getModel(), Boolean.TRUE);
                                }

                                // 非中文：走 Ollama 评估流程
//                                ChatRequest llmReq = buildEvaluateRequest();
                                Flux<OllamaChatResponse> originalLlmStream = ollamaClient.chat(sentence, userInput, chatReq)
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

                                            Flux<OllamaChatResponse> replyStream = toTokenStream(finalReply, chatReq.getModel(), Boolean.FALSE);
                                            return replyStream.concatWithValues(eval.getFinalMetadata());
                                        });
                            });

                })
                .switchIfEmpty(
                        sentenceRepoService.initUserFirstSentence(userId)
                                .flatMapMany(m -> toTokenStream("下一句：" + m, chatReq.getModel(), Boolean.TRUE))
                );
    }


    private Flux<OllamaChatResponse> toTokenStream(String fullContent, String model, boolean done) {
        List<String> tokens = CustomStringUtil.splitByLengthStream(fullContent, 5);

        Flux<OllamaChatResponse> tokenFlux = Flux.fromIterable(tokens)
                .delayElements(Duration.ofMillis(1))
                .map(token -> OllamaChatResponse.builder()
                        .message(OllamaMessageDTO.builder().content(token).build())
                        .model(model)
                        .created_at(Instant.now())
                        .done(false)
                        .build()
                );

        if (!done) {
            return tokenFlux;
        }

        OllamaChatResponse doneFrame = OllamaChatResponse.builder()
                .message(OllamaMessageDTO.builder().content("").build())
                .model(model)
                .created_at(Instant.now())
                .done(true)
                .done_reason("stop")
                .eval_count((long) tokens.size())
                .build();

        return tokenFlux.concatWithValues(doneFrame);
    }

}
