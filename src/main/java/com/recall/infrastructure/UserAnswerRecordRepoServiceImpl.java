package com.recall.infrastructure;

import com.recall.domain.UserAnswerRecordDO;
import com.recall.infrastructure.repository.UserAnswerRecordRepoService;
import com.recall.infrastructure.repository.UserAnswerRecordRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserAnswerRecordRepoServiceImpl implements UserAnswerRecordRepoService {


    @Resource
    private UserAnswerRecordRepository repository;


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
