package com.recall.dto.resp;

import com.recall.dto.req.OllamaMessageDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ChatResponse {
    private String model;
    private String created_at;
    private OllamaMessageDTO message;
    private boolean done;
}
