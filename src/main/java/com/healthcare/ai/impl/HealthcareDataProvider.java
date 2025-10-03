package com.healthcare.ai.impl;

import com.healthcare.ai.DataProvider;
import com.healthcare.model.Facility;
import com.healthcare.model.Facility.FacilityType;
import com.healthcare.model.Patient;
import com.healthcare.repository.FacilityRepository;
import com.healthcare.repository.PatientRepository;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HealthcareDataProvider implements DataProvider {

    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;

    @Override
    public List<Map<String, Object>> getSamplePatients(int count) {
        try {
            List<Patient> patients = patientRepository.findPatientsByDeletedAtIsNull(PageRequest.of(0, count));

            return patients.stream()
                .map(this::convertToPatientMap)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting sample patients", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> searchPatients(String searchTerm, Long facilityId, int limit) {
        try {
            List<Patient> patients;

            if (facilityId != null && searchTerm != null) {
                patients = patientRepository.searchPatientsByFacility(facilityId, searchTerm, PageRequest.of(0, limit)).getContent();
            } else if (searchTerm != null) {
                patients = patientRepository.searchPatients(searchTerm, PageRequest.of(0, limit)).getContent();
            } else if (facilityId != null) {
                patients = patientRepository.findByFacilityIdAndDeletedAtIsNull(facilityId, PageRequest.of(0, limit)).getContent();
            } else {
                patients = patientRepository.findPatientsByDeletedAtIsNull(PageRequest.of(0, limit));
            }

            return patients.stream()
                .map(this::convertToSearchResultMap)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching patients", e);
            return List.of();
        }
    }

    @Override
    public long getPatientCount() {
        try {
            return patientRepository.countByDeletedAtIsNull();
        } catch (Exception e) {
            log.error("Error getting patient count", e);
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> getFacilities(String type, int limit) {
        try {
            List<Facility> facilities;

            if (type != null) {
                facilities = facilityRepository.findByTypeAndIsActiveTrue(
                    Facility.FacilityType.valueOf(type), PageRequest.of(0, limit)).getContent();
            } else {
                facilities = facilityRepository.findByIsActiveTrue(PageRequest.of(0, limit)).getContent();
            }

            return facilities.stream()
                .map(this::convertToFacilityMap)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting facilities", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getFacilitiesWithPatientCounts(int limit) {
        try {
            List<Facility> facilities = facilityRepository.findByIsActiveTrue(PageRequest.of(0, limit)).getContent();

            return facilities.stream()
                .map(facility -> {
                    Long patientCount = facilityRepository.countPatientsByFacilityId(facility.getId());
                    return Map.<String, Object>of(
                        "id", facility.getId(),
                        "name", facility.getName(),
                        "type", facility.getType().toString(),
                        "patientCount", patientCount != null ? patientCount : 0,
                        "address", facility.getAddress()
                    );
                }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting facilities with patient counts", e);
            return List.of();
        }
    }

    @Override
    public Map<String, Long> getFacilityStats() {
        try {
            Map<FacilityType, Long> result = new HashMap<>();

            for (Object[] row : facilityRepository.countFacilitiesByType()) {
                result.put((FacilityType) row[0], (Long) row[1]);
            }

            return result.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().toString(),
                    Map.Entry::getValue
                ));
        } catch (Exception e) {
            log.error("Error getting facility stats", e);
            return Map.of();
        }
    }

    @Override
    public long getFacilityCount() {
        try {
            return facilityRepository.countByIsActiveTrue();
        } catch (Exception e) {
            log.error("Error getting facility count", e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getSystemStats() {
        try {
            long totalPatients = getPatientCount();
            long totalFacilities = getFacilityCount();
            Map<String, Long> facilitiesByType = getFacilityStats();

            return Map.of(
                "totalPatients", totalPatients,
                "totalFacilities", totalFacilities,
                "facilitiesByType", facilitiesByType,
                "averagePatientsPerFacility", totalFacilities > 0 ? (double) totalPatients / totalFacilities : 0
            );
        } catch (Exception e) {
            log.error("Error getting system stats", e);
            return Map.of("error", "Unable to retrieve system statistics");
        }
    }

    private Map<String, Object> convertToPatientMap(Patient patient) {
        return Map.of(
            "id", patient.getId(),
            "firstName", patient.getFirstName(),
            "lastName", patient.getLastName(),
            "facility", patient.getFacility() != null ? patient.getFacility().getName() : "",
            "facilityType", patient.getFacility() != null ?  patient.getFacility().getType().toString() : "",
            "medicalRecordNumber", maskSensitiveInfo(patient.getMedicalRecordNumber()),
            "age", calculateAge(patient.getDateOfBirth()),
            "email", maskEmail(patient.getEmail())
        );
    }

    private Map<String, Object> convertToSearchResultMap(Patient patient) {
        return Map.of(
            "id", patient.getId(),
            "name", patient.getFirstName() + " " + patient.getLastName(),
            "facility", patient.getFacility().getName(),
            "facilityType", patient.getFacility().getType().toString(),
            "medicalRecordNumber", maskSensitiveInfo(patient.getMedicalRecordNumber())
        );
    }

    private Map<String, Object> convertToFacilityMap(Facility facility) {
        return Map.of(
            "id", facility.getId(),
            "name", facility.getName(),
            "type", facility.getType().toString(),
            "address", facility.getAddress()
        );
    }

    private String maskSensitiveInfo(String value) {
        if (value == null || value.length() <= 4) return "***";
        return "***" + value.substring(value.length() - 4);
    }

    private String maskEmail(String email) {
        if (email == null) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***" + email.substring(atIndex);
    }

    private int calculateAge(LocalDate birthDate) {
        return birthDate != null ? Period.between(birthDate, LocalDate.now()).getYears() : 0;
    }
}