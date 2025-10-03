package com.healthcare.controller;

import com.healthcare.ai.AIChatOrchestrator;
import com.healthcare.dto.ChatRequest;
import com.healthcare.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final AIChatOrchestrator aiChatOrchestrator;

    @PostMapping
    public ResponseEntity<ChatResponse> processChat(@Valid @RequestBody ChatRequest chatRequest) {
        try {
            log.info("Received chat request: {}", chatRequest.getQuery());
            ChatResponse response = aiChatOrchestrator.processQuery(chatRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.ok(ChatResponse.error(
                "I apologize, but an unexpected error occurred. Please try again later."));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, AIChatOrchestrator.ServiceInfo>> getServiceStatus() {
        try {
            Map<String, AIChatOrchestrator.ServiceInfo> status = aiChatOrchestrator.getServiceStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting service status", e);
            return ResponseEntity.ok(Map.of("error",
                new AIChatOrchestrator.ServiceInfo(false,
                    com.healthcare.ai.ServiceHealth.UNHEALTHY, "Status Check Failed")));
        }
    }

    @GetMapping
    public ResponseEntity<ChatResponse> processSimpleQuery(@RequestParam String q) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuery(q);
        return processChat(chatRequest);
    }
}