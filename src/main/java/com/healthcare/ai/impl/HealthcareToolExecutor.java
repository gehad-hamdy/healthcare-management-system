package com.healthcare.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.DataProvider;
import com.healthcare.ai.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class HealthcareToolExecutor implements ToolExecutor {

    private final DataProvider dataProvider;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(String functionName, Map<String, Object> arguments) throws JsonProcessingException {
        try {
            log.info("Executing function: {} with arguments: {}", functionName, arguments);

            switch (functionName) {
                case "get_sample_patients":
                    return executeGetSamplePatients(arguments);
                case "search_patients":
                    return executeSearchPatients(arguments);
                case "get_facilities":
                    return executeGetFacilities(arguments);
                case "get_facilities_with_patient_counts":
                    return executeGetFacilitiesWithPatientCounts(arguments);
                case "get_system_stats":
                    return executeGetSystemStats(arguments);
                case "get_patient_count":
                    return executeGetPatientCount(arguments);
                case "get_facility_count":
                    return executeGetFacilityCount(arguments);
                default:
                    return createErrorResponse("Function not implemented: " + functionName);
            }
        } catch (Exception e) {
            log.error("Error executing function: {}", functionName, e);
            return createErrorResponse("Error executing function: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(String functionName) {
        return getSupportedFunctions().contains(functionName);
    }

    @Override
    public List<String> getSupportedFunctions() {
        return List.of(
            "get_sample_patients",
            "search_patients",
            "get_facilities",
            "get_facilities_with_patient_counts",
            "get_system_stats",
            "get_patient_count",
            "get_facility_count"
        );
    }

    private String executeGetSamplePatients(Map<String, Object> arguments) throws JsonProcessingException {
        int count = arguments.containsKey("count") ? ((Number) arguments.get("count")).intValue() : 3;
        List<Map<String, Object>> patients = dataProvider.getSamplePatients(count);
        return objectMapper.writeValueAsString(patients);
    }

    private String executeSearchPatients(Map<String, Object> arguments) throws JsonProcessingException {
        String searchTerm = (String) arguments.get("search_term");
        Long facilityId = arguments.containsKey("facility_id") ?
            ((Number) arguments.get("facility_id")).longValue() : null;

        List<Map<String, Object>> patients = dataProvider.searchPatients(searchTerm, facilityId, 10);

        return objectMapper.writeValueAsString(Map.of(
            "count", patients.size(),
            "patients", patients
        ));
    }

    private String executeGetFacilities(Map<String, Object> arguments) throws JsonProcessingException {
        String type = (String) arguments.get("type");
        List<Map<String, Object>> facilities = dataProvider.getFacilities(type, 20);
        return objectMapper.writeValueAsString(facilities);
    }

    private String executeGetFacilitiesWithPatientCounts(Map<String, Object> arguments) throws JsonProcessingException {
        int limit = arguments.containsKey("limit") ? ((Number) arguments.get("limit")).intValue() : 20;
        List<Map<String, Object>> facilities = dataProvider.getFacilitiesWithPatientCounts(limit);
        return objectMapper.writeValueAsString(facilities);
    }

    private String executeGetSystemStats(Map<String, Object> arguments) throws JsonProcessingException {
        Map<String, Object> stats = dataProvider.getSystemStats();
        return objectMapper.writeValueAsString(stats);
    }

    private String executeGetPatientCount(Map<String, Object> arguments) throws JsonProcessingException {
        long count = dataProvider.getPatientCount();
        return objectMapper.writeValueAsString(Map.of("patientCount", count));
    }

    private String executeGetFacilityCount(Map<String, Object> arguments) throws JsonProcessingException {
        long count = dataProvider.getFacilityCount();
        return objectMapper.writeValueAsString(Map.of("facilityCount", count));
    }

    private String createErrorResponse(String error) throws JsonProcessingException {
        return objectMapper.writeValueAsString(Map.of("error", error));
    }
}