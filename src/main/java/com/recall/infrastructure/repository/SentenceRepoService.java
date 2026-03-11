package com.recall.infrastructure.repository;

import com.recall.domain.SentenceDO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service interface for sentence-related database operations.
 */
public interface SentenceRepoService {
    /**
     * Loads the content of a sentence by its ID.
     * @param sentenceId The ID of the sentence
     * @return A Mono emitting the sentence content
     */
    Mono<String> loadSentence(Long sentenceId);

    /**
     * Initializes the first sentence for a user.
     * @param userId The ID of the user
     * @return A Mono emitting the content of the first sentence
     */
    Mono<String> initUserFirstSentence(Long userId);

    /**
     * Gets the next sentence after the given sentence ID.
     * @param sentenceId The current sentence ID
     * @return A Mono emitting the next sentence DO
     */
    Mono<SentenceDO> getNextSentence(Long sentenceId);
}
