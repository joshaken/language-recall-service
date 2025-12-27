package com.recall.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("sentences")
@Getter
@Setter
@NoArgsConstructor
public class SentenceDO {

    @Id
    private Long id;

    private String sentenceType;

    private String content;

    private Integer level;

    private LocalDateTime createTime;

}

