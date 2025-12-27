package com.recall.infrastructure;

import com.recall.infrastructure.repository.UserRepoService;
import com.recall.infrastructure.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserRepoServiceImpl implements UserRepoService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private R2dbcEntityTemplate template;

    @Override
    public Mono<Long> findUserCurrentSentence(Long userId) {
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
