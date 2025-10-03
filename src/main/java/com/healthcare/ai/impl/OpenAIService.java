package com.healthcare.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.AIService;
import com.healthcare.ai.ServiceHealth;
import com.healthcare.ai.ToolExecutor;
import com.healthcare.dto.ChatRequest;
import com.healthcare.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAIService implements AIService {

    @Value("${app.ai.openai.enabled:true}")
    private boolean enabled;

    @Value("${app.ai.openai.api-key:}")
    private String apiKey;

    @Value("${app.ai.openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${app.ai.openai.model:gpt-3.5-turbo}")
    private String model;

    private final ObjectMapper objectMapper;
    private final ToolExecutor toolExecutor;

    private boolean healthy = true;
    private String lastError;

    @Override
    public ChatResponse processQuery(ChatRequest chatRequest) {
        if (!isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OpenAI service is disabled");
        }

        try {
            log.info("Processing query with OpenAI: {}", chatRequest.getQuery());
            return processWithOpenAI(chatRequest.getQuery());
        } catch (Exception e) {
            log.error("OpenAI service error", e);
            healthy = false;
            lastError = e.getMessage();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "OpenAI service error: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public String getServiceName() {
        return "OpenAI GPT Service";
    }

    @Override
    public ServiceHealth getHealth() {
        if (!isEnabled()) return ServiceHealth.DISABLED;
        return healthy ? ServiceHealth.HEALTHY : ServiceHealth.UNHEALTHY;
    }

    private ChatResponse processWithOpenAI(String query) throws Exception {
        List<Map<String, Object>> tools = createToolDefinitions();
        Map<String, Object> request = createChatRequest(query, tools);

        String responseBody = makeApiCall(request);
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

        return processApiResponse(response, query);
    }

    private Map<String, Object> createChatRequest(String query, List<Map<String, Object>> tools) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
            createSystemMessage(),
            createUserMessage(query)
        ));
        request.put("tools", tools);
        request.put("tool_choice", "auto");
        request.put("max_tokens", 1000);
        request.put("temperature", 0.1);

        return request;
    }

    private Map<String, Object> createSystemMessage() {
        return Map.of("role", "system", "content", """
                You are a healthcare management assistant. You help users query patient and facility data.
                Use the provided tools to fetch actual data from the system.
                
                Guidelines:
                - Be concise and helpful
                - Use real data from the system when available
                - Maintain patient privacy - never expose full medical record numbers or sensitive information
                - If you can't find specific data, suggest alternative queries
                - Format responses clearly with bullet points when appropriate
                """);
    }

    private Map<String, Object> createUserMessage(String content) {
        return Map.of("role", "user", "content", content);
    }

    private List<Map<String, Object>> createToolDefinitions() {
        return List.of(
            createTool("get_sample_patients",
                "Get sample patient profiles from the healthcare system",
                Map.of("count", Map.of(
                    "type", "integer",
                    "description", "Number of sample patients to retrieve, default is 3"
                ))),

            createTool("search_patients",
                "Search patients by name, facility, or other criteria",
                Map.of(
                    "search_term", Map.of("type", "string", "description", "Search term for patient name, email, or medical record number"),
                    "facility_id", Map.of("type", "integer", "description", "Filter by specific facility ID")
                )),

            createTool("get_facilities",
                "Get list of healthcare facilities with their details",
                Map.of("type", Map.of(
                    "type", "string",
                    "description", "Filter by facility type",
                    "enum", List.of("HOSPITAL", "CLINIC", "LAB", "PHARMACY", "OTHER")
                ))),

            createTool("get_facilities_with_patient_counts",
                "Get facilities with their patient counts for analysis",
                Map.of("limit", Map.of(
                    "type", "integer",
                    "description", "Maximum number of facilities to return, default is 20"
                ))),

            createTool("get_system_stats",
                "Get system statistics including patient counts and facility counts",
                Map.of()),

            createTool("get_patient_count",
                "Get total number of patients in the system",
                Map.of()),

            createTool("get_facility_count",
                "Get total number of facilities in the system",
                Map.of())
        );
    }

    private Map<String, Object> createTool(String name, String description, Map<String, Object> parameters) {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", name,
                "description", description,
                "parameters", Map.of(
                    "type", "object",
                    "properties", parameters,
                    "required", List.of()
                )
            )
        );
    }

    private String makeApiCall(Map<String, Object> requestBody) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String requestJson = objectMapper.writeValueAsString(requestBody);

        log.debug("OpenAI API Request: {}", requestJson);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        log.debug("OpenAI API Response Status: {}", response.statusCode());

        if (response.statusCode() != 200) {
            handleApiError(response);
        }

        return response.body();
    }

    private void handleApiError(HttpResponse<String> response) throws Exception {
        String errorBody = response.body();
        log.error("OpenAI API error: {}", errorBody);

        try {
            Map<String, Object> errorResponse = objectMapper.readValue(errorBody, Map.class);
            Map<String, Object> error = (Map<String, Object>) errorResponse.get("error");
            String errorMessage = (String) error.get("message");
            String errorType = (String) error.get("type");

            throw new RuntimeException("OpenAI API error (" + errorType + "): " + errorMessage);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API call failed with status " +
                response.statusCode() + ": " + errorBody);
        }
    }

    private ChatResponse processApiResponse(Map<String, Object> response, String originalQuery) throws Exception {
        Map<String, Object> message = extractMessage(response);

        if (message.containsKey("tool_calls")) {
            return handleToolCalls(originalQuery, message);
        } else {
            return handleDirectResponse(message);
        }
    }

    private Map<String, Object> extractMessage(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> firstChoice = choices.get(0);
        return (Map<String, Object>) firstChoice.get("message");
    }

    private ChatResponse handleToolCalls(String originalQuery, Map<String, Object> message) throws Exception {
        List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");
        Map<String, Object> firstToolCall = toolCalls.get(0);
        Map<String, Object> function = (Map<String, Object>) firstToolCall.get("function");

        String functionName = (String) function.get("name");
        Map<String, Object> functionArgs = objectMapper.readValue((String) function.get("arguments"), Map.class);

        // Execute the tool
        String toolResult = toolExecutor.execute(functionName, functionArgs);

        // Get final response from OpenAI with tool results
        return getFinalResponse(originalQuery, toolResult, message, firstToolCall);
    }

    private ChatResponse getFinalResponse(String originalQuery, String toolResult,
        Map<String, Object> message, Map<String, Object> toolCall) throws Exception {
        List<Map<String, Object>> messages = List.of(
            createSystemMessage(),
            createUserMessage(originalQuery),
            message,
            createToolMessage(toolCall, toolResult)
        );

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", messages);
        request.put("max_tokens", 500);

        String responseBody = makeApiCall(request);
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        Map<String, Object> finalMessage = extractMessage(response);

        String content = (String) finalMessage.get("content");
        Object structuredData = objectMapper.readValue(toolResult, Object.class);

        healthy = true; // Mark as healthy after successful call

        return ChatResponse.success(content, "openai", structuredData);
    }

    private Map<String, Object> createToolMessage(Map<String, Object> toolCall, String content) {
        return Map.of(
            "role", "tool",
            "tool_call_id", toolCall.get("id"),
            "content", content
        );
    }

    private ChatResponse handleDirectResponse(Map<String, Object> message) {
        String content = (String) message.get("content");
        healthy = true;
        return ChatResponse.success(content, "openai_direct", null);
    }

    public String getLastError() {
        return lastError;
    }
}