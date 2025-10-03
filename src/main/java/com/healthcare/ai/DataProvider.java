package com.healthcare.ai;

import java.util.List;
import java.util.Map;

public interface DataProvider {
    // Patient methods
    List<Map<String, Object>> getSamplePatients(int count);
    List<Map<String, Object>> searchPatients(String searchTerm, Long facilityId, int limit);
    long getPatientCount();

    // Facility methods
    List<Map<String, Object>> getFacilities(String type, int limit);
    List<Map<String, Object>> getFacilitiesWithPatientCounts(int limit);
    Map<String, Long> getFacilityStats();
    long getFacilityCount();

    // System methods
    Map<String, Object> getSystemStats();
}