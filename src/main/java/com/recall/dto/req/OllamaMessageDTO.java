package com.recall.dto.req;

import lombok.*;

/**
 * DTO representing a message in the chat conversation with Ollama.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class OllamaMessageDTO {
    /** The role of the message sender (e.g., "system", "user", "assistant"). */
    private String role;

    /** The content of the message. */
    private String content;

    /** Optional thinking/reasoning content from the model. */
    private String thinking;
}
