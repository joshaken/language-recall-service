package com.recall.utils;

import com.recall.dto.req.OllamaMessageDTO;
import com.recall.dto.resp.OllamaChatResponse;

public class OllamaChatUtil {

    public static OllamaChatResponse createChunk(String content) {
        return OllamaChatResponse.builder()
                .message(OllamaMessageDTO.builder().content(content).build())
                .done(false)
                .build();
    }

    public static OllamaChatResponse createDoneChunk() {
        return OllamaChatResponse.builder()
                .message(OllamaMessageDTO.builder().content("").build())
                .done(true)
                .done_reason("stop")
                .build();
    }
}
