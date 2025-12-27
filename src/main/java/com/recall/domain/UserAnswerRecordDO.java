package com.recall.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("user_answer_record")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAnswerRecordDO {

    @Id
    private Long id;

    private Long sentenceId;

    private Long userId;

    private Boolean correct;

    private LocalDateTime createTime;

}

