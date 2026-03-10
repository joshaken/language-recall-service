package com.recall.dto.resp;

import com.recall.dto.req.OllamaMessageDTO;
import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class OllamaChatResponse {
    private String model;
    private Instant createdAt;
    private OllamaMessageDTO message;
    private boolean done;
    private String doneReason;

    private Long totalDuration;
    private Long loadDuration;
    private Integer promptEvalCount;
    private Long promptEvalDuration;
    private Integer evalCount;
    private Long evalDuration;
}
