package com.recall.infrastructure.repository;

import com.recall.domain.UserDO;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Repository interface for UserDO entities.
 */
public interface UserRepository extends ReactiveCrudRepository<UserDO, Long> {
}
