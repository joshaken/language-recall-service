package com.recall.infrastructure.repository;

import com.recall.domain.UserAnswerRecordDO;
import reactor.core.publisher.Mono;

public interface UserAnswerRecordRepoService {
    Mono<UserAnswerRecordDO> saveResult(Long userId, Long sentenceId, Boolean correct);
}
