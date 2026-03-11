package com.recall.infrastructure;

import com.recall.domain.UserAnswerRecordDO;
import com.recall.infrastructure.repository.UserAnswerRecordRepoService;
import com.recall.infrastructure.repository.UserAnswerRecordRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Implementation of UserAnswerRecordRepoService for database operations.
 */
@Service
@Slf4j
public class UserAnswerRecordRepoServiceImpl implements UserAnswerRecordRepoService {


    @Resource
    private UserAnswerRecordRepository repository;


    /**
     * Saves the result of a user's answer.
     * @param userId The ID of the user
     * @param sentenceId The ID of the sentence answered
     * @param correct Whether the answer was correct
     * @return A Mono emitting the saved record
     */
    @Override
    public Mono<UserAnswerRecordDO> saveResult(Long userId, Long sentenceId, Boolean correct) {
        log.info("userId[{}},sentenceId[{}],correct[{}]", userId, sentenceId, correct);
        if (correct == null) {
            correct = Boolean.FALSE;
        }
        return repository.save(
                UserAnswerRecordDO.builder()
                        .userId(userId)
                        .sentenceId(sentenceId)
                        .correct(correct)
                        .createTime(LocalDateTime.now())
                        .build()
        );
    }
}
