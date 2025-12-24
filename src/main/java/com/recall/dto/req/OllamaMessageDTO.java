package com.recall.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class OllamaMessageDTO {
    private String role;    // system | user | assistant
    private String content;
    private String thinking;
}
