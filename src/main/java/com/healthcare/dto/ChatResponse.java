package com.healthcare.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatResponse {
    private String answer;
    private String source;
    private Object data;
    private boolean error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static ChatResponse error(String message) {
        return ChatResponse.builder()
            .answer(message)
            .source("SYSTEM_ERROR")
            .error(true)
            .build();
    }

    public static ChatResponse success(String answer, String source, Object data) {
        return ChatResponse.builder()
            .answer(answer)
            .source(source)
            .data(data)
            .error(false)
            .build();
    }
}