package com.recall.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Data Object representing a sentence entity.
 * Maps to the "sentences" table in the database.
 */
@Table("sentences")
@Getter
@Setter
@NoArgsConstructor
public class SentenceDO {

    @Id
    private Long id;

    /** The category or type of the sentence (e.g., grammar, vocabulary). */
    private String sentenceType;

    /** The actual text content of the sentence. */
    private String content;

    /** The difficulty level of the sentence. */
    private Integer level;

    /** The timestamp when the sentence was created. */
    private LocalDateTime createTime;

    /**
     * Constructor for creating a sentence with ID and content.
     * @param id The unique identifier
     * @param content The sentence text
     */
    public SentenceDO(Long id, String content) {
        this.id = id;
        this.content = content;
    }
}

