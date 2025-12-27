package com.recall.infrastructure.repository;

import reactor.core.publisher.Mono;

public interface UserRepoService {

    Mono<Long> findUserCurrentSentence(Long userId);

    Mono<?> updateCurrentSentence(Long userId, Long sentenceId);
}
