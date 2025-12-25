package com.recall.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    private String username;

    private Long currentSentenceId;

    private LocalDateTime createTime;

    // getter / setter
}

