package com.recall.dto.resp;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EvaluationResult {

    private Boolean correct;

    private String modelAnswer;

    private ChatResponse finalMetadata;
}
