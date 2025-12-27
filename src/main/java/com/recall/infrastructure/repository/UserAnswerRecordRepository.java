package com.recall.infrastructure.repository;

import com.recall.domain.UserAnswerRecordDO;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserAnswerRecordRepository extends ReactiveCrudRepository<UserAnswerRecordDO, Long> {
}
