package com.recall.dto.req;

import lombok.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ChatRequest {

    private String model;
    private List<OllamaMessageDTO> messages;
    private Map<String, Object> options;
    private Boolean stream;
}
