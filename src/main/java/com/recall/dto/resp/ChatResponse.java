package com.recall.dto.resp;

import com.recall.dto.req.OllamaMessageDTO;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ChatResponse {
    private String model;
    private Instant created_at;
    private OllamaMessageDTO message;
    private boolean done;
    private String done_reason;

    private Long total_duration;
    private Long load_duration;
    private Long prompt_eval_count;
    private Long prompt_eval_duration;
    private Long eval_count;
    private Long eval_duration;
}
