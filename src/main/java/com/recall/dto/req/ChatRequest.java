package com.recall.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ChatRequest {

    private String model;
    private List<OllamaMessageDTO> messages;
    private Map<String, Object> options;
    private String stream;
}
