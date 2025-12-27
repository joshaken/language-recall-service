package com.recall.infrastructure;

import com.recall.domain.SentenceDO;
import com.recall.infrastructure.repository.SentenceRepoService;
import com.recall.infrastructure.repository.SentenceRepository;
import jakarta.annotation.Resource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SentenceRepoServiceImpl implements SentenceRepoService {

    @Resource
    private R2dbcEntityTemplate template;

    @Resource
    private SentenceRepository sentenceRepository;

    @Override
    public Mono<String> loadSentence(Long sentenceId) {
        return template.getDatabaseClient()
                .sql("""
                            SELECT CONTENT
                            FROM sentences
                            WHERE id = :id
                        """)
                .bind("id", sentenceId)
                .map(row -> row.get("CONTENT", String.class))
                .one();
    }

    @Override
    public Mono<String> initUserFirstSentence(Long userId) {
        return template.getDatabaseClient()
                .sql("""
                            SELECT id, content
                            FROM sentences
                            order by ID limit 1
                        """)
                .map((row, meta) -> {
                    SentenceDO sentence = new SentenceDO();
                    sentence.setId(row.get("id", Long.class));
                    sentence.setContent(row.get("content", String.class));
                    return sentence;
                })
                .one()
                .flatMap(sentence -> template.getDatabaseClient()
                        .sql("""
                                    UPDATE users
                                    SET current_sentence_id = :sentenceId
                                    WHERE id = :userId
                                """)
                        .bind("sentenceId", sentence.getId())
                        .bind("userId", userId)
                        .fetch()
                        .rowsUpdated()
                        .thenReturn(sentence.getContent())
                );
    }

    @Override
    public Mono<SentenceDO> getNextSentence(Long sentenceId) {
        return sentenceRepository.findNextSentence(sentenceId);
    }
}
