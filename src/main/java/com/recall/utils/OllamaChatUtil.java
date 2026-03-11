package com.recall.utils;

import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.OllamaChatResponse;

/**
 * Utility class for creating Ollama chat response chunks.
 */
public class OllamaChatUtil {

    /**
     * Creates a non-final response chunk with the given content.
     * @param content The content of the chunk
     * @return The created OllamaChatResponse
     */
    public static OllamaChatResponse createChunk(String content) {
        return OllamaChatResponse.builder()
                .message(OllamaMessageDTO.builder().content(content).build())
                .done(false)
                .build();
    }

    /**
     * Creates a final response chunk indicating the end of the stream.
     * @return The created OllamaChatResponse
     */
    public static OllamaChatResponse createDoneChunk() {
        return OllamaChatResponse.builder()
                .message(OllamaMessageDTO.builder().content("").build())
                .done(true)
                .doneReason("stop")
                .build();
    }
}
