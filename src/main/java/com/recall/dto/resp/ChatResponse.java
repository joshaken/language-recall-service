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
    private Integer load_duration;
    private Integer prompt_eval_count;
    private Integer prompt_eval_duration;
    private Integer eval_count;
    private Integer eval_duration;
}
