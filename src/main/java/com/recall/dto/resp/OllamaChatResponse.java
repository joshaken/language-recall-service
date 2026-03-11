package com.recall.dto.resp;

import com.recall.dto.req.OllamaMessageDTO;
import lombok.*;

import java.time.Instant;

/**
 * DTO representing a chat response from Ollama.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class OllamaChatResponse {
    /** The model used to generate the response. */
    private String model;

    /** The timestamp when the response was created. */
    private Instant createdAt;

    /** The message content of the response. */
    private OllamaMessageDTO message;

    /** Indicates if this is the final chunk of the response. */
    private boolean done;

    /** The reason why the generation stopped (e.g., "stop", "length"). */
    private String doneReason;

    /** Total duration of the request in nanoseconds. */
    private Long totalDuration;

    /** Duration to load the model in nanoseconds. */
    private Long loadDuration;

    /** Number of tokens in the prompt. */
    private Integer promptEvalCount;

    /** Time spent evaluating the prompt in nanoseconds. */
    private Long promptEvalDuration;

    /** Number of tokens in the response. */
    private Integer evalCount;

    /** Time spent evaluating the response in nanoseconds. */
    private Long evalDuration;
}
