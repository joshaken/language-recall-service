package com.recall.dto.resp;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EvaluationResult {

    /**
     * 是否翻译正确（语法 + 语义）
     */
    private Boolean correct;

    /**
     * 评分（0 ~ 100）
     */
//    private Integer score;

    /**
     * 改进建议
     */
//    private List<Object> suggestions;

//    private String modelAnswer;
}
