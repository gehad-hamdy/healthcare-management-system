package com.healthcare.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.ChatRequest;
import com.healthcare.dto.ChatResponse;
import com.healthcare.service.PatientQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIChatService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    private final ObjectMapper objectMapper;
    private final PatientQueryService patientQueryService;

    public ChatResponse processQuery(ChatRequest chatRequest) {
        try {
            String query = chatRequest.getQuery().toLowerCase();

            // Handle specific query patterns with database data
            ChatResponse databaseResponse = handleDatabaseQueries(query);
            if (databaseResponse != null) {
                return databaseResponse;
            }

            // Use AI for complex queries if enabled and API key available
            if (aiEnabled && openaiApiKey != null && !openaiApiKey.trim().isEmpty()) {
                try {
                    return processWithOpenAI(chatRequest);
                } catch (Exception e) {
                    log.warn("OpenAI API call failed, falling back to local logic", e);
                    return processWithLocalLogic(query);
                }
            } else {
                return processWithLocalLogic(query);
            }
        } catch (Exception e) {
            log.error("Error processing AI query", e);
            return ChatResponse.of(
                "I apologize, but I'm having trouble processing your request right now. Please try again later.",
                "ERROR"
            );
        }
    }

    private ChatResponse handleDatabaseQueries(String query) {
        try {
            if (query.contains("statistic") || query.contains("count") || query.contains("how many")) {
                Map<String, Object> patientStats = patientQueryService.getPatientStatistics();
                Map<String, Object> facilityStats = patientQueryService.getFacilityStatistics();

                String answer = String.format(
                    "Here are the current system statistics:\n\n" +
                        "Patients:\n- Total: %d\n- Active: %d\n- Deleted: %d\n\n" +
                        "Facilities:\n- Total: %d\n- Active: %d\n- Inactive: %d",
                    patientStats.get("totalPatients"),
                    patientStats.get("activePatients"),
                    patientStats.get("deletedPatients"),
                    facilityStats.get("totalFacilities"),
                    facilityStats.get("activeFacilities"),
                    facilityStats.get("inactiveFacilities")
                );

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("patientStatistics", patientStats);
                metadata.put("facilityStatistics", facilityStats);

                return ChatResponse.builder()
                    .answer(answer)
                    .data(Arrays.asList(patientStats, facilityStats))
                    .timestamp(LocalDateTime.now())
                    .source("DATABASE")
                    .metadata(metadata)
                    .build();
            }

            // ... rest of the method remains the same
        } catch (Exception e) {
            log.error("Error handling database query", e);
        }

        return null;
    }

    private String extractFacilityType(String query) {
        if (query.contains("hospital")) return "HOSPITAL";
        if (query.contains("clinic")) return "CLINIC";
        if (query.contains("lab") || query.contains("laboratory")) return "LAB";
        if (query.contains("pharmacy")) return "PHARMACY";
        return null;
    }

    private ChatResponse processWithOpenAI(ChatRequest chatRequest) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        // Get system context from database
        Map<String, Object> systemContext = getSystemContext();

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
            "role", "system",
            "content", createSystemPrompt(systemContext)
        ));
        messages.add(Map.of("role", "user", "content", chatRequest.getQuery()));

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(openaiApiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + openaiApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");

            return ChatResponse.builder()
                .answer(message.get("content"))
                .timestamp(LocalDateTime.now())
                .source("OPENAI")
                .metadata(Map.of(
                    "model", responseMap.get("model"),
                    "tokensUsed", responseMap.get("usage")
                ))
                .build();
        } else {
            throw new RuntimeException("OpenAI API returned status: " + response.statusCode());
        }
    }

    private String createSystemPrompt(Map<String, Object> systemContext) {
        return String.format("""
            You are a healthcare management assistant. You help users query patient and facility data.
            
            Current System Context:
            - Total Patients: %d
            - Active Patients: %d
            - Total Facilities: %d
            - Active Facilities: %d
            
            Be concise and helpful. Focus on providing accurate information about the healthcare management system.
            For data queries, explain what type of information is available and how to query it.
            If asked about specific data that might be in the system, suggest using the appropriate API endpoints.
            
            Available endpoints:
            - GET /api/patients - List patients with search and pagination
            - GET /api/facilities - List facilities with filtering
            - GET /api/facilities/{id}/patients - Get patients by facility
            - POST /api/chat - This AI query endpoint
            
            Always be truthful about data availability and don't invent patient or facility details.
            """,
            ((Number) systemContext.get("totalPatients")).longValue(),
            ((Number) systemContext.get("activePatients")).longValue(),
            ((Number) systemContext.get("totalFacilities")).longValue(),
            ((Number) systemContext.get("activeFacilities")).longValue()
        );
    }

    private Map<String, Object> getSystemContext() {
        Map<String, Object> patientStats = patientQueryService.getPatientStatistics();
        Map<String, Object> facilityStats = patientQueryService.getFacilityStatistics();

        Map<String, Object> context = new HashMap<>();
        context.put("totalPatients", patientStats.get("totalPatients"));
        context.put("activePatients", patientStats.get("activePatients"));
        context.put("totalFacilities", facilityStats.get("totalFacilities"));
        context.put("activeFacilities", facilityStats.get("activeFacilities"));

        return context;
    }

    private ChatResponse processWithLocalLogic(String query) {
        String lowerQuery = query.toLowerCase();

        if (lowerQuery.contains("hello") || lowerQuery.contains("hi") || lowerQuery.contains("hey")) {
            return ChatResponse.of(
                "Hello! I'm your healthcare management assistant. I can help you query patient and facility data. " +
                    "You can ask me about system statistics, sample data, or how to use the API endpoints.",
                "SYSTEM"
            );
        }

        if (lowerQuery.contains("help") || lowerQuery.contains("what can you do")) {
            return ChatResponse.of("""
                    I can help you with:
                    
                    Patient Information:
                    - Search and list patients
                    - View patient statistics
                    - Get sample patient profiles
                    
                    Facility Management:
                    - List healthcare facilities
                    - View facility statistics
                    - Find patients by facility type
                    
                    System Operations:
                    - Get current system statistics
                    - Explain available API endpoints
                    
                    Try asking:
                    - "Show me system statistics"
                    - "Give me sample patient profiles"
                    - "How many patients are in the system?"
                    - "List patients in hospital facilities"
                    """, "SYSTEM");
        }

        if (lowerQuery.contains("endpoint") || lowerQuery.contains("api") || lowerQuery.contains("how to")) {
            return ChatResponse.of("""
                    Available API Endpoints:
                    
                    Patient Management:
                    - GET /api/patients - List all patients (supports search, pagination)
                    - GET /api/patients/{id} - Get specific patient details
                    - POST /api/patients - Create new patient
                    - PUT /api/patients/{id} - Update patient
                    - DELETE /api/patients/{id} - Soft delete patient
                    - GET /api/facilities/{id}/patients - Get patients by facility
                    
                    Facility Management:
                    - GET /api/facilities - List all facilities
                    - GET /api/facilities/{id} - Get facility details with patient count
                    - POST /api/facilities - Create new facility
                    - PUT /api/facilities/{id} - Update facility
                    - DELETE /api/facilities/{id} - Soft delete facility
                    
                    AI Query:
                    - POST /api/chat - Natural language queries (this endpoint)
                    """, "SYSTEM");
        }

        // Default response for unrecognized queries
        return ChatResponse.of(
            "I understand you're asking about: '" + query + "'. " +
                "I can help you query patient data, facility information, or system statistics. " +
                "Try asking about 'system statistics', 'sample patients', or type 'help' for more options.",
            "SYSTEM"
        );
    }
}