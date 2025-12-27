package com.recall.infrastructure.repository;

import com.recall.domain.SentenceDO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SentenceRepository extends ReactiveCrudRepository<SentenceDO, Long> {

    @Query("""
                SELECT *
                FROM sentences
                WHERE id > :currentId
                ORDER BY id ASC
                LIMIT 1
            """)
    Mono<SentenceDO> findNextSentence(@Param("currentId") Long currentId);
}
