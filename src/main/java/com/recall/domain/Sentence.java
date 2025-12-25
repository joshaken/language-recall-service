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
public class Sentence {

    @Id
    private Long id;

    private String sentenceType;

    private String content;

    private Integer level;

    // 可选
    private LocalDateTime createTime;

    // getter / setter
}

