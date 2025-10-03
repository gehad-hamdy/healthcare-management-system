package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Chat request with natural language query")
public class ChatRequest {

    @NotBlank(message = "Query cannot be blank")
    @Schema(description = "Natural language query about patients or facilities",
        example = "List sample patient profiles")
    private String query;
}