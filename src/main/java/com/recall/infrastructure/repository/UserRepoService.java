package com.recall.infrastructure.repository;

import reactor.core.publisher.Mono;

/**
 * Service interface for user-related database operations.
 */
public interface UserRepoService {

    /**
     * Finds the current sentence ID for a given user.
     * @param userId The ID of the user
     * @return A Mono emitting the current sentence ID, or empty if not found
     */
    Mono<Long> findUserCurrentSentence(Long userId);

    /**
     * Updates the current sentence for a given user.
     * @param userId The ID of the user
     * @param sentenceId The ID of the new current sentence
     * @return A Mono that completes when the update is done
     */
    Mono<?> updateCurrentSentence(Long userId, Long sentenceId);
}
