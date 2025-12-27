package com.recall.dto.req;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class OllamaMessageDTO {
    private String role;    // system | user | assistant
    private String content;
    private String thinking;
}
