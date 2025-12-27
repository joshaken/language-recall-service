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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
        //取最后一条用户输入
        OllamaMessageDTO lastMessage = chatReq.getMessages()
                .stream().filter(x -> "user".equals(x.getRole()))
                .toList()
                .getLast();
        String userInput = lastMessage.getContent();

        Long userId = 1L;

        //查用户当前句子
        Mono<Long> currentSentenceMono =
                userRepoService.findUserCurrentSentence(userId);
        //如果是中文，返回sentence
        //如果不是中文，拿到sentence 一起送给LLM
        //拿到评价结果， 记录结果
        //拿到下一个句子
        //修改user表的当前句子ID
        //返回评价结果和新句子
        return currentSentenceMono
                .flatMapMany(sentenceId -> {
                    log.info("user sentenceId {}", sentenceId);
                    //加载当前句子
                    return sentenceRepoService.loadSentence(sentenceId)
                            .flatMapMany(sentence -> {

                                // ===== 分支 1：用户输入是中文 =====
                                if (!CustomStringUtil.containsJapanese(userInput)) {
                                    return Flux.just(addResp(chatReq, sentence));
                                }

                                // ===== 分支 2：非中文 → 送给 LLM =====
                                ChatRequest llmReq =
                                        buildEvaluateRequest(sentence, userInput, chatReq);

                                Flux<ChatResponse> llmStream = ollamaClient.chat(llmReq)
                                        .cache();

                                Mono<EvaluationResult> lastEvalMono =
                                        llmStream.takeUntil(ChatResponse::isDone) // 关键
                                                .map(chunk -> chunk.getMessage() != null
                                                        ? chunk.getMessage().getContent() : "")
                                                .filter(StringUtils::hasText)
                                                .reduce(new StringBuilder(), StringBuilder::append)
                                                .map(StringBuilder::toString)
                                                .mapNotNull(json -> JsonUtil.toObject(json, EvaluationResult.class));

                                return lastEvalMono.flatMapMany(eval -> {

                                    // 保存学习结果
                                    Mono<UserAnswerRecordDO> saveResultMono =
                                            userAnswerRecordRepoService.saveResult(
                                                    userId,
                                                    sentenceId,
                                                    eval.getCorrect()
                                            );

                                    // 获取下一句
                                    Mono<SentenceDO> nextSentenceMono =
                                            sentenceRepoService.getNextSentence(sentenceId);

                                    // 更新用户当前句子
                                    Mono<Void> updateUserMono =
                                            nextSentenceMono
                                                    .flatMap(next ->
                                                            userRepoService.updateCurrentSentence(
                                                                    userId,
                                                                    next.getId()
                                                            )
                                                    )
                                                    .then();

                                    // 返回响应
                                    return Mono.when(saveResultMono, updateUserMono)
                                            .thenMany(
                                                    nextSentenceMono.map(next ->
                                                            addResp(chatReq,
                                                                    buildFinalContent(eval, next.getContent())
                                                            )
                                                    )
                                            );
                                });
                            });
                })
                // 用户还没有当前句子
                .switchIfEmpty(
                        sentenceRepoService.initUserFirstSentence(userId)
                                .flatMapMany(sen ->
                                        Flux.just(
                                                addResp(chatReq, sen)
                                        )
                                )
                );

    }

    private String buildFinalContent(EvaluationResult eval, String nextSentence) {
        return """
                评价结果：
                是否正确：%s
                评分：%d
                反馈：%s
                                
                %s
                """.formatted(
                eval.getCorrect() ? "正确" : "不正确",
                eval.getScore(),
                JsonUtil.toJson(eval.getSuggestions()),
                nextSentence
        );
    }


    private static ChatResponse addResp(ChatRequest chatReq, String sentence) {
        return ChatResponse.builder()
                .model(chatReq.getModel())
                .message(
                        OllamaMessageDTO.builder()
                                .role("assistant")
                                .content("下一句：" + sentence)
                                .build()
                )
                .created_at(Instant.now())
                .done(true)
                .done_reason("stop")
                .build();
    }


    private ChatRequest buildEvaluateRequest(String sentence, String userInput, ChatRequest chatReq) {
        return ChatRequest.builder()
                .model(chatReq.getModel())
                .messages(List.of(
                        OllamaMessageDTO.builder()
                                .role("system")
                                .content("""
                                        你是一个日语人。
                                        你的任务是评估外国人对给定中文句子的日语翻译。

                                        请从以下方面进行评价：
                                        1. 语法是否正确
                                        2. 助词使用是否恰当
                                        3. 表达是否自然、符合语境

                                        请只返回 JSON。
                                        JSON 格式如下：

                                        {
                                          "correct": "ture | false",
                                          "naturalness": "natural | unnatural",
                                          "score": 0-100,
                                          "suggestions": [
                                            {
                                              "style": "polite | casual | natural",
                                              "sentence": "",
                                              "explanation": ""
                                            }
                                          ]
                                        }
                                                        """)
                                .build(),

                        OllamaMessageDTO.builder()
                                .role("user")
                                .content("""
                                        【中文原句】
                                        %s

                                        【学习者翻译】
                                        %s
                                                        """.formatted(sentence, userInput))
                                .build()
                ))
                .stream(true)
                .build();
    }

}
