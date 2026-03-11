package com.recall.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Data Object representing a user's answer record.
 * Maps to the "user_answer_record" table in the database.
 */
@Table("user_answer_record")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAnswerRecordDO {

    @Id
    private Long id;

    /** The ID of the sentence being answered. */
    private Long sentenceId;

    /** The ID of the user who answered. */
    private Long userId;

    /** Indicates whether the answer was correct. */
    private Boolean correct;

    /** The timestamp when the record was created. */
    private LocalDateTime createTime;

}

