package com.recall.infrastructure.repository;

import com.recall.domain.SentenceDO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SentenceRepoService {
    Mono<String> loadSentence(Long sentenceId);

    Mono<String> initUserFirstSentence(Long userId);

    Mono<SentenceDO> getNextSentence(Long sentenceId);
}
