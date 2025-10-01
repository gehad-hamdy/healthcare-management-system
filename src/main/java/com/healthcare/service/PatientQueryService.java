package com.healthcare.service;

import com.healthcare.model.Patient;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PatientQueryService {

    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;

    public Map<String, Object> getPatientStatistics() {
        long totalPatients = patientRepository.count();
        long activePatients = patientRepository.findByDeletedAtIsNull(org.springframework.data.domain.Pageable.unpaged())
            .getTotalElements();

        return Map.of(
            "totalPatients", totalPatients,
            "activePatients", activePatients,
            "deletedPatients", totalPatients - activePatients
        );
    }

    public Map<String, Object> getFacilityStatistics() {
        long totalFacilities = facilityRepository.count();
        long activeFacilities = facilityRepository.findByIsActiveTrue(org.springframework.data.domain.Pageable.unpaged())
            .getTotalElements();

        return Map.of(
            "totalFacilities", totalFacilities,
            "activeFacilities", activeFacilities,
            "inactiveFacilities", totalFacilities - activeFacilities
        );
    }

    public List<Map<String, Object>> getSamplePatientProfiles(int count) {
        return patientRepository.findByDeletedAtIsNull(org.springframework.data.domain.PageRequest.of(0, count))
            .stream()
            .map(this::convertToProfileMap)
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPatientsByFacilityType(String facilityType) {
        return patientRepository.findByDeletedAtIsNull(org.springframework.data.domain.Pageable.unpaged())
            .stream()
            .filter(patient -> patient.getFacility().getType().name().equalsIgnoreCase(facilityType))
            .map(this::convertToProfileMap)
            .collect(Collectors.toList());
    }

    private Map<String, Object> convertToProfileMap(Patient patient) {
        return Map.of(
            "id", patient.getId(),
            "firstName", patient.getFirstName(),
            "lastName", patient.getLastName(),
            "email", patient.getEmail() != null ? patient.getEmail() : "N/A",
            "phone", patient.getPhone() != null ? patient.getPhone() : "N/A",
            "dateOfBirth", patient.getDateOfBirth().toString(),
            "gender", patient.getGender() != null ? patient.getGender().name() : "UNSPECIFIED",
            "medicalRecordNumber", patient.getMedicalRecordNumber() != null ? patient.getMedicalRecordNumber() : "N/A",
            "facility", Map.of(
                "id", patient.getFacility().getId(),
                "name", patient.getFacility().getName(),
                "type", patient.getFacility().getType().name()
            ),
            "age", calculateAge(patient.getDateOfBirth())
        );
    }

    private int calculateAge(java.time.LocalDate birthDate) {
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }
}
