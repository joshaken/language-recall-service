package com.recall.infrastructure.repository;

import com.recall.domain.UserDO;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<UserDO, Long> {
}
