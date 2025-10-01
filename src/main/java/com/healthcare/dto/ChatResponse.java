package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {
    private String answer;
    private String sessionId;
    private LocalDateTime timestamp;
    private List<Map<String, Object>> data;
    private String source;
    private Map<String, Object> metadata;
    private String error;

    public static ChatResponse of(String answer) {
        return ChatResponse.builder()
            .answer(answer)
            .timestamp(LocalDateTime.now())
            .source("SYSTEM")
            .build();
    }

    public static ChatResponse of(String answer, List<Map<String, Object>> data) {
        return ChatResponse.builder()
            .answer(answer)
            .data(data)
            .timestamp(LocalDateTime.now())
            .source("DATABASE")
            .build();
    }

    public static ChatResponse of(String answer, String source) {
        return ChatResponse.builder()
            .answer(answer)
            .timestamp(LocalDateTime.now())
            .source(source)
            .build();
    }

    public static ChatResponse error(String errorMessage) {
        return ChatResponse.builder()
            .error(errorMessage)
            .timestamp(LocalDateTime.now())
            .source("ERROR")
            .build();
    }
}