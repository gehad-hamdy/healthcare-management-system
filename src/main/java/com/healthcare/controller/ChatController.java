package com.healthcare.controller;

import com.healthcare.dto.ChatRequest;
import com.healthcare.dto.ChatResponse;
import com.healthcare.ai.AIChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final AIChatService aiChatService;

    @PostMapping
    public ResponseEntity<ChatResponse> processQuery(@Valid @RequestBody ChatRequest chatRequest) {
        log.info("Received chat query: {}", chatRequest.getQuery());

        try {
            ChatResponse response = aiChatService.processQuery(chatRequest);
            log.info("Chat response generated with source: {}", response.getSource());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat query", e);

            ChatResponse errorResponse = ChatResponse.builder()
                .answer("I apologize, but I encountered an error while processing your request. Please try again later.")
                .source("ERROR")
                .build();

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chat endpoint is healthy and ready to process queries");
    }
}