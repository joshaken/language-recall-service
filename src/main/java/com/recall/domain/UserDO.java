package com.recall.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Data Object representing a user entity.
 * Maps to the "users" table in the database.
 */
@Table("users")
@Getter
@Setter
@NoArgsConstructor
public class UserDO {

    @Id
    private Long id;

    /** The username of the user. */
    private String username;

    /** The ID of the sentence the user is currently on. */
    private Long currentSentenceId;

    /** The total number of sentences the user has encountered. */
    private Long sentenceCount;

    /** The mode the user is in (e.g., practice, test). */
    private Integer mode;

    /** The timestamp when the user was created. */
    private LocalDateTime createTime;

}

