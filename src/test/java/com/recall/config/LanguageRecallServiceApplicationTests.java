package com.recall.config;

import com.recall.domain.SentenceDO;
import com.recall.domain.UserAnswerRecordDO;
import com.recall.dto.req.ChatRequest;
import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.OllamaChatResponse;
import com.recall.infrastructure.repository.OllamaClient;
import com.recall.infrastructure.repository.SentenceRepoService;
import com.recall.infrastructure.repository.UserAnswerRecordRepoService;
import com.recall.infrastructure.repository.UserRepoService;
import com.recall.service.IChatService;
import com.recall.utils.OllamaChatUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LanguageRecallServiceApplicationTests {
    @MockBean
    private UserRepoService userRepoService;
    @Autowired
    private IChatService chatService;
    @MockBean
    private SentenceRepoService sentenceRepoService;
    @MockBean
    private OllamaClient ollamaClient;
    @MockBean
    private UserAnswerRecordRepoService userAnswerRecordRepoService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnNextSentenceDirectlyForChineseInput() {
        // Given
        when(userRepoService.findUserCurrentSentence(1L)).thenReturn(Mono.just(1L));
        when(sentenceRepoService.initUserFirstSentence(1L)).thenReturn(Mono.just("你好1"));
        when(sentenceRepoService.loadSentence(1L)).thenReturn(Mono.just("你好2"));

        ChatRequest req = new ChatRequest();
        req.setMessages(List.of(OllamaMessageDTO.builder().role("user").content("hello").build()));

        // When
        Flux<OllamaChatResponse> result = chatService.chat(req);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(r -> r.getMessage().getContent().contains("下一句"))
                .expectNextCount(1) // 没有中间段
                .assertNext(r -> {
                    assertThat(r.isDone()).isTrue();
                    assertThat(r.getDone_reason()).isEqualTo("stop");
                })
                .expectComplete()
                .verify();
    }

    @Test
    void shouldEvaluateJapaneseInputAndSaveResult() {
        // Mock LLM response stream
        Flux<OllamaChatResponse> mockLlmStream = Flux.just(
                OllamaChatUtil.createChunk("{"),
                OllamaChatUtil.createChunk("\"correct\": false"),
                OllamaChatUtil.createChunk("}"),
                OllamaChatUtil.createChunk("haha，u are actually right"),
                OllamaChatUtil.createDoneChunk()
        );

        when(ollamaClient.chat(any(), any(), any())).thenReturn(mockLlmStream);
        when(userRepoService.findUserCurrentSentence(1L)).thenReturn(Mono.just(1L));
        when(sentenceRepoService.loadSentence(1L)).thenReturn(Mono.just("你好"));
        when(sentenceRepoService.getNextSentence(1L)).thenReturn(Mono.just(new SentenceDO(2L, "再见")));
        when(userAnswerRecordRepoService.saveResult(eq(1L), eq(1L), eq(false))).thenReturn(Mono.just(new UserAnswerRecordDO()));
        when(userRepoService.updateCurrentSentence(eq(1L), eq(2L))).thenReturn(Mono.empty());
        when(sentenceRepoService.initUserFirstSentence(1L)).thenReturn(Mono.just("你好1"));

        ChatRequest req = new ChatRequest();
        req.setModel("llama3");
        req.setMessages(List.of(OllamaMessageDTO.builder().role("user").content("こんにちは").build())); // 日语

        Flux<OllamaChatResponse> result = chatService.chat(req);

        StepVerifier.create(result)
                .recordWith(ArrayList::new)
                .expectNextCount(8)
                .expectRecordedMatches(chunks ->
                        chunks.stream()
                                .map(r -> Optional.ofNullable(r.getMessage())
                                        .map(OllamaMessageDTO::getContent)
                                        .orElse(""))
                                .anyMatch(content -> content.contains("下一句") || content.contains("再见"))
                )
                .verifyComplete();

        // Verify side effects were called
        verify(userAnswerRecordRepoService).saveResult(1L, 1L, false);
        verify(userRepoService).updateCurrentSentence(1L, 2L);
    }

    @Test
    void shouldInitializeUserSentenceWhenNotFound() {
        when(userRepoService.findUserCurrentSentence(1L)).thenReturn(Mono.empty());
        when(sentenceRepoService.initUserFirstSentence(1L)).thenReturn(Mono.just("初次见面"));

    }

    @Test
    void shouldReturnBadRequestOnInvalidJson() {
        String invalidJson = "{ not valid }";

        webTestClient
                .post().uri("/api/chat") // 假设你的路由是 /chat
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> assertThat(errorMessage).contains("Error"));
    }

    @Test
    void springAIResponse() {

        // given
//        ChatRequest req = new ChatRequest();
//        req.setModel("qwen3-vl:4b");
//
//        // when
//        Flux<OllamaChatResponse> result =
//                ollamaClient.chat2("你好", "こんにちは", req);
////
////        // then
//        StepVerifier.create(result)
//
//                .verifyComplete();
    }


}
