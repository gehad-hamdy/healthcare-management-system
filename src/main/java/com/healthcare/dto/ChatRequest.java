package com.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "Query cannot be blank")
    @Size(min = 1, max = 1000, message = "Query must be between 1 and 1000 characters")
    private String query;

    private String sessionId;
    private Map<String, Object> context;

    public ChatRequest(String query) {
        this.query = query;
    }
}