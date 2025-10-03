package com.healthcare.ai.impl;

import com.healthcare.ai.AIService;
import com.healthcare.ai.DataProvider;
import com.healthcare.ai.ServiceHealth;
import com.healthcare.dto.ChatRequest;
import com.healthcare.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalAIService implements AIService {

    private final DataProvider dataProvider;

    @Override
    public ChatResponse processQuery(ChatRequest chatRequest) {
        try {
            String query = chatRequest.getQuery().toLowerCase().trim();
            log.info("Processing query with Local AI: {}", query);

            if (query.contains("sample") || query.contains("example") || query.contains("show me patient")) {
                return handleSamplePatients();
            } else if (query.contains("analyze") || query.contains("distribution") || query.contains("analysis")) {
                return handleAnalysis(query);
            } else if (query.contains("facility") || query.contains("hospital") || query.contains("clinic")) {
                return handleFacilities(query);
            } else if (query.contains("stat") || query.contains("count") || query.contains("how many")) {
                return handleStatistics();
            } else if (query.contains("search") && query.contains("patient")) {
                return handlePatientSearch(query);
            } else if (query.contains("help")) {
                return handleHelp();
            } else {
                return handleGeneralResponse();
            }
        } catch (Exception e) {
            log.error("Local AI service error", e);
            return ChatResponse.error("I encountered an error while processing your request: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return true; // Always available as fallback
    }

    @Override
    public String getServiceName() {
        return "Local AI Service";
    }

    @Override
    public ServiceHealth getHealth() {
        return ServiceHealth.HEALTHY;
    }

    private ChatResponse handleSamplePatients() {
        List<Map<String, Object>> patients = dataProvider.getSamplePatients(3);

        if (patients.isEmpty()) {
            return ChatResponse.success(
                "I don't have any sample patient profiles available at the moment. " +
                    "You can add patients using the POST /api/patients endpoint.",
                "local",
                null
            );
        }

        String answer = String.format(
            "Here are %d sample patient profiles from our system:\n\n" +
                "For more detailed patient information, you can use:\n" +
                "- GET /api/patients - List all patients\n" +
                "- GET /api/patients/{id} - Get specific patient details\n" +
                "- POST /api/patients - Register new patient",
            patients.size()
        );

        return ChatResponse.success(answer, "local", patients);
    }

    private ChatResponse handleAnalysis(String query) {
        if (query.contains("patient distribution") || query.contains("facility type")) {
            return analyzePatientDistribution();
        } else {
            return handleGeneralAnalysis();
        }
    }

    private ChatResponse analyzePatientDistribution() {
        Map<String, Object> stats = dataProvider.getSystemStats();
        long totalPatients = (Long) stats.get("totalPatients");
        long totalFacilities = (Long) stats.get("totalFacilities");
        Map<String, Long> facilitiesByType = (Map<String, Long>) stats.get("facilitiesByType");

        StringBuilder analysis = new StringBuilder();
        analysis.append("**Patient Distribution Analysis**\n\n");

        analysis.append(String.format("? Total Patients: %d\n", totalPatients));
        analysis.append(String.format("? Total Facilities: %d\n", totalFacilities));
        analysis.append(String.format("? Average Patients per Facility: %.1f\n\n", stats.get("averagePatientsPerFacility")));

        analysis.append("**Facilities by Type:**\n");
        facilitiesByType.forEach((type, count) -> {
            analysis.append(String.format("? %s: %d facilities\n", type, count));
        });

        analysis.append("\n**Available Analysis Endpoints:**\n");
        analysis.append("- GET /api/facilities - Facility details\n");
        analysis.append("- GET /api/patients - Patient listings\n");
        analysis.append("- GET /api/facilities/{id}/patients - Patients by facility");

        return ChatResponse.success(analysis.toString(), "local", stats);
    }

    private ChatResponse handleFacilities(String query) {
        String type = extractFacilityType(query);
        List<Map<String, Object>> facilities = dataProvider.getFacilities(type, 10);

        String typeText = type != null ? type.toLowerCase() + " " : "";
        String answer = String.format(
            "Here are the %sfacilities in our system:\n\n" +
                "Use these endpoints for facility management:\n" +
                "- GET /api/facilities - List all facilities\n" +
                "- GET /api/facilities/{id} - Get facility details\n" +
                "- GET /api/facilities/{id}/patients - Get patients by facility",
            typeText
        );

        return ChatResponse.success(answer, "local", facilities);
    }

    private ChatResponse handleStatistics() {
        Map<String, Object> stats = dataProvider.getSystemStats();

        String answer = String.format(
            "**Healthcare System Statistics**\n\n" +
                "? Total Patients: %d\n" +
                "? Total Facilities: %d\n" +
                "? Average Patients per Facility: %.1f\n\n" +
                "The system is actively managing healthcare data.",
            stats.get("totalPatients"),
            stats.get("totalFacilities"),
            stats.get("averagePatientsPerFacility")
        );

        return ChatResponse.success(answer, "local", stats);
    }

    private ChatResponse handlePatientSearch(String query) {
        String searchTerm = extractSearchTerm(query);
        List<Map<String, Object>> patients = dataProvider.searchPatients(searchTerm, null, 5);

        String answer = String.format(
            "Found %d patients matching your search.\n\n" +
                "For advanced search, use:\n" +
                "GET /api/patients?search=%s",
            patients.size(), searchTerm
        );

        return ChatResponse.success(answer, "local", patients);
    }

    private ChatResponse handleHelp() {
        String help = """
                **Healthcare Management Assistant**
                
                I can help you with:
                
                **Patient Information:**
                ? "Show sample patient profiles"
                ? "Search for patients"
                ? "Patient statistics"
                
                **Facility Information:**
                ? "List hospitals/clinics"
                ? "Facility analysis"
                ? "Patient distribution"
                
                **System Information:**
                ? "System statistics"
                ? "Patient counts"
                ? "Facility overview"
                
                **Example Queries:**
                ? "List sample patient profiles"
                ? "Analyze patient distribution"
                ? "How many facilities are there?"
                ? "Show me all hospitals"
                """;

        return ChatResponse.success(help, "local", null);
    }

    private ChatResponse handleGeneralResponse() {
        String response = """
                I'm your healthcare management assistant. I can help you query:
                ? Patient records and profiles
                ? Healthcare facilities
                ? System statistics and analysis
                
                Try asking about:
                ? "Sample patient profiles"
                ? "Healthcare facilities"
                ? "System statistics"
                ? Or type "help" for more options
                """;

        return ChatResponse.success(response, "local", null);
    }

    private ChatResponse handleGeneralAnalysis() {
        String analysis = """
                **Analysis Capabilities**
                
                I can help analyze:
                ? Patient distribution across facilities
                ? Facility capacity and utilization
                ? System-wide healthcare metrics
                
                Try these analysis queries:
                ? "Analyze patient distribution by facility type"
                ? "Facility capacity analysis"
                ? "Patient load distribution"
                """;

        return ChatResponse.success(analysis, "local", null);
    }

    private String extractFacilityType(String query) {
        if (query.contains("hospital")) return "HOSPITAL";
        if (query.contains("clinic")) return "CLINIC";
        if (query.contains("lab")) return "LAB";
        if (query.contains("pharmacy")) return "PHARMACY";
        return null;
    }

    private String extractSearchTerm(String query) {
        if (query.contains("named")) {
            return query.substring(query.indexOf("named") + 5).trim();
        }
        if (query.contains("search for")) {
            return query.substring(query.indexOf("search for") + 10).trim();
        }
        return "patient";
    }
}