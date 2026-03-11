package com.recall.infrastructure.repository;

import com.recall.domain.UserAnswerRecordDO;
import reactor.core.publisher.Mono;

/**
 * Service interface for user answer record operations.
 */
public interface UserAnswerRecordRepoService {
    /**
     * Saves the result of a user's answer.
     * @param userId The ID of the user
     * @param sentenceId The ID of the sentence answered
     * @param correct Whether the answer was correct
     * @return A Mono emitting the saved record
     */
    Mono<UserAnswerRecordDO> saveResult(Long userId, Long sentenceId, Boolean correct);
}
