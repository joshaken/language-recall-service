package com.recall.infrastructure;

import com.recall.infrastructure.repository.UserRepoService;
import com.recall.infrastructure.repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of UserRepoService for database operations.
 */
@Service
@Slf4j
public class UserRepoServiceImpl implements UserRepoService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private R2dbcEntityTemplate template;

    /**
     * Finds the current sentence ID for a given user.
     * @param userId The ID of the user
     * @return A Mono emitting the current sentence ID
     */
    @Override
    public Mono<Long> findUserCurrentSentence(Long userId) {
        log.info("DB lookup for userId={}", userId);
        return template.getDatabaseClient()
                .sql("""
                            SELECT current_sentence_id
                            FROM users
                            WHERE id = :id
                        """)
                .bind("id", userId)
                .map(row -> row.get("current_sentence_id", Long.class))
                .one();
    }

    /**
     * Updates the current sentence for a given user.
     * @param userId The ID of the user
     * @param sentenceId The ID of the new current sentence
     * @return A Mono that completes when the update is done
     */
    @Override
    public Mono<?> updateCurrentSentence(Long userId, Long sentenceId) {
        return template.getDatabaseClient()
                .sql("""
                            UPDATE users
                            SET current_sentence_id = :sentenceId
                            WHERE id = :userId
                        """)
                .bind("sentenceId", sentenceId)
                .bind("userId", userId)
                .fetch()
                .rowsUpdated();
    }
}
