package com.recall.dto.req;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO representing a chat request to the service.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ChatRequest {

    /** The model identifier to use for chat completion. */
    private String model;

    /** The list of messages in the conversation. */
    private List<OllamaMessageDTO> messages;

    /** Additional options for the model. */
    private Map<String, Object> options;

    /** Whether to stream the response. */
    private Boolean stream;
}
