package com.recall.service.impl;

import com.recall.domain.SentenceDO;
import com.recall.domain.UserAnswerRecordDO;
import com.recall.dto.req.ChatRequest;
import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.LlmAccumulator;
import com.recall.dto.resp.OllamaChatResponse;
import com.recall.infrastructure.ai.OllamaClient;
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

    // Default user ID for single-user mode or when user context is not available
    private static final Long DEFAULT_USER_ID = 1L;

    // Chunk size for splitting text into tokens in the simulated stream
    private static final int TOKEN_CHUNK_SIZE = 5;

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
        // Extract the last user message from the request
        OllamaMessageDTO lastMessage = chatReq.getMessages()
                .stream()
                .filter(x -> "user".equals(x.getRole()))
                .reduce((a, b) -> b) // Get the last user message
                .orElseThrow(() -> new IllegalArgumentException("No user message found"));

        String userInput = lastMessage.getContent();
        Long userId = DEFAULT_USER_ID; // TODO: Replace with dynamic user ID extraction (e.g., from JWT or session)

        return userRepoService.findUserCurrentSentence(userId)
                .flatMapMany(sentenceId -> {
                    log.info("Found current sentence ID {} for user {}", sentenceId, userId);
                    return sentenceRepoService.loadSentence(sentenceId)
                            .flatMapMany(sentence -> {
                                // If user input is NOT Japanese (i.e., Chinese or English), return next sentence directly
                                if (!CustomStringUtil.containsJapanese(userInput)) {
                                    return handleDirectNextSentence(sentence, chatReq.getModel());
                                }

                                // If user input IS Japanese, process through Ollama for evaluation
                                return handleJapaneseEvaluation(sentence, userInput, userId, sentenceId, chatReq);
                            });
                })
                .switchIfEmpty(
                        // If user has no current sentence, initialize with the first sentence
                        sentenceRepoService.initUserFirstSentence(userId)
                                .flatMapMany(m -> handleDirectNextSentence(m, chatReq.getModel()))
                );
    }

    /**
     * Handles the case where the user input is not Japanese (Chinese/English).
     * Returns a stream of tokens for the next sentence.
     */
    private Flux<OllamaChatResponse> handleDirectNextSentence(String sentence, String model) {
        String content = "下一句：" + sentence;
        OllamaChatResponse doneFrame = OllamaChatResponse.builder()
                .message(OllamaMessageDTO.builder().content("").build())
                .model(model)
                .createdAt(Instant.now())
                .done(true)
                .doneReason("stop")
                .build();
        return toTokenStream(content, model)
                .concatWithValues(doneFrame);
    }

    /**
     * Handles the complex evaluation flow when the user input is Japanese.
     * 1. Calls Ollama to evaluate the input against the current sentence.
     * 2. Accumulates the response to parse EvaluationResult.
     * 3. Saves the user's answer record.
     * 4. Gets the next sentence and updates the user's current sentence.
     * 5. Returns the model's answer combined with the next sentence.
     */
    private Flux<OllamaChatResponse> handleJapaneseEvaluation(String currentSentence, String userInput, Long userId, Long sentenceId, ChatRequest chatReq) {
        // Call Ollama for evaluation
        Mono<LlmAccumulator> accumMono = ollamaClient.chat(currentSentence, userInput, chatReq)
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
                .cache();

        // Process the evaluation result: save record, get next sentence, update user state
        return accumMono
                .flatMap(accum -> {
                    Mono<SentenceDO> nextSentenceMono = sentenceRepoService
                            .getNextSentence(sentenceId)
                            .cache();

                    Mono<Void> saveMono = userAnswerRecordRepoService
                            .saveResult(userId, sentenceId, accum.getCorrect())
                            .then();

                    Mono<Void> updateMono = nextSentenceMono
                            .flatMap(next -> userRepoService.updateCurrentSentence(userId, next.getId()))
                            .then();

                    // Wait for save and update to complete, then prepare the final reply
                    return Mono.when(saveMono, updateMono)
                            .then(nextSentenceMono)
                            .map(next -> {
                                String finalReply = accum.getCleanedAnswer() + "\n\n下一句：" + next.getContent();
                                return Tuples.of(accum, finalReply);
                            });
                })
                .flatMapMany(tuple -> {
                    LlmAccumulator accum = tuple.getT1();
                    String finalReply = tuple.getT2();

                    // Stream the final reply as tokens, then append the final metadata frame
                    return toTokenStream(finalReply, chatReq.getModel())
                            .concatWithValues(accum.getFinalMetadata());
                });
    }


    /**
     * Converts a full string content into a simulated token stream.
     * Splits the content into chunks of fixed length and emits them with a small delay.
     *
     * @param fullContent The content to stream
     * @param model       The model name to include in the response
     * @return A Flux of OllamaChatResponse objects
     */
    private Flux<OllamaChatResponse> toTokenStream(String fullContent, String model) {
        List<String> tokens = CustomStringUtil.splitByLengthStream(fullContent, TOKEN_CHUNK_SIZE);

        return Flux.fromIterable(tokens)
                .delayElements(Duration.ofMillis(1))
                .map(token -> OllamaChatResponse.builder()
                        .message(OllamaMessageDTO.builder().content(token).build())
                        .model(model)
                        .createdAt(Instant.now())
                        .done(false)
                        .build()
                );
    }

}
