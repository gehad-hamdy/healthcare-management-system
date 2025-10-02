package com.healthcare.service.impl;

import com.healthcare.service.PatientService;

import com.healthcare.dto.PatientDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.dto.PatientSearchDTO;
import com.healthcare.model.Facility;
import com.healthcare.model.Patient;
import com.healthcare.repository.FacilityRepository;
import com.healthcare.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;

    @Override
    public Page<PatientDTO> getAllPatients(PageRequestDTO pageRequest, String search) {
        Pageable pageable = createPageable(pageRequest);
        Page<Patient> patients;

        if (search != null && !search.trim().isEmpty()) {
            patients = patientRepository.searchPatients(search, pageable);
        } else {
            patients = patientRepository.findByDeletedAtIsNull(pageable);
        }

        return patients.map(this::convertToDTO);
    }

    @Override
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));
        return convertToDTO(patient);
    }

    @Transactional
    @Override
    public PatientDTO createPatient(PatientDTO patientDTO) {
        // Check if facility exists and is active
        Facility facility = facilityRepository.findByIdAndIsActiveTrue(patientDTO.getFacilityId())
            .orElseThrow(() -> new EntityNotFoundException("Facility not found or inactive with id: " + patientDTO.getFacilityId()));

        // Check if medical record number is unique
        if (patientDTO.getMedicalRecordNumber() != null &&
            patientRepository.existsByMedicalRecordNumberAndDeletedAtIsNull(patientDTO.getMedicalRecordNumber())) {
            throw new IllegalArgumentException("Medical record number already exists: " + patientDTO.getMedicalRecordNumber());
        }

        Patient patient = Patient.builder()
            .facility(facility)
            .firstName(patientDTO.getFirstName())
            .lastName(patientDTO.getLastName())
            .email(patientDTO.getEmail())
            .phone(patientDTO.getPhone())
            .dateOfBirth(patientDTO.getDateOfBirth())
            .gender(patientDTO.getGender())
            .medicalRecordNumber(patientDTO.getMedicalRecordNumber())
            .address(patientDTO.getAddress())
            .build();

        Patient saved = patientRepository.save(patient);
        return convertToDTO(saved);
    }

    @Transactional
    @Override
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        Patient patient = patientRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));

        // If facility is being updated, validate new facility
        if (!patient.getFacility().getId().equals(patientDTO.getFacilityId())) {
            Facility newFacility = facilityRepository.findByIdAndIsActiveTrue(patientDTO.getFacilityId())
                .orElseThrow(() -> new EntityNotFoundException("Facility not found or inactive with id: " + patientDTO.getFacilityId()));
            patient.setFacility(newFacility);
        }

        // Check if medical record number is unique (if being changed)
        if (patientDTO.getMedicalRecordNumber() != null &&
            !patientDTO.getMedicalRecordNumber().equals(patient.getMedicalRecordNumber()) &&
            patientRepository.existsByMedicalRecordNumberAndDeletedAtIsNull(patientDTO.getMedicalRecordNumber())) {
            throw new IllegalArgumentException("Medical record number already exists: " + patientDTO.getMedicalRecordNumber());
        }

        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setEmail(patientDTO.getEmail());
        patient.setPhone(patientDTO.getPhone());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setMedicalRecordNumber(patientDTO.getMedicalRecordNumber());
        patient.setAddress(patientDTO.getAddress());

        Patient updated = patientRepository.save(patient);
        return convertToDTO(updated);
    }

    @Transactional
    @Override
    public void softDeletePatient(Long id) {
        Patient patient = patientRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));

        patient.setDeletedAt(LocalDateTime.now());
        patientRepository.save(patient);
    }

    @Override
    public Page<PatientDTO> getPatientsByFacility(Long facilityId, PageRequestDTO pageRequest, String search) {
        Pageable pageable = createPageable(pageRequest);
        Page<Patient> patients;

        if (search != null && !search.trim().isEmpty()) {
            patients = patientRepository.searchPatientsByFacility(facilityId, search, pageable);
        } else {
            patients = patientRepository.findByFacilityIdAndDeletedAtIsNull(facilityId, pageable);
        }

        return patients.map(this::convertToDTO);
    }

    @Override
    public Page<PatientDTO> searchPatients(PageRequestDTO pageRequest, PatientSearchDTO searchDTO) {
        Pageable pageable = createPageable(pageRequest);
        Page<Patient> patients = patientRepository.findAll((root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            // Only non-deleted patients
            predicates = criteriaBuilder.and(predicates,
                criteriaBuilder.isNull(root.get("deletedAt")));

            if (searchDTO.getFirstName() != null && !searchDTO.getFirstName().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),
                        "%" + searchDTO.getFirstName().toLowerCase() + "%"));
            }

            if (searchDTO.getLastName() != null && !searchDTO.getLastName().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),
                        "%" + searchDTO.getLastName().toLowerCase() + "%"));
            }

            if (searchDTO.getEmail() != null && !searchDTO.getEmail().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                        "%" + searchDTO.getEmail().toLowerCase() + "%"));
            }

            if (searchDTO.getFacilityId() != null) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("facility").get("id"), searchDTO.getFacilityId()));
            }

            if (searchDTO.getMedicalRecordNumber() != null && !searchDTO.getMedicalRecordNumber().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("medicalRecordNumber")),
                        "%" + searchDTO.getMedicalRecordNumber().toLowerCase() + "%"));
            }

            return predicates;
        }, pageable);

        return patients.map(this::convertToDTO);
    }

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setFacilityId(patient.getFacility().getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setMedicalRecordNumber(patient.getMedicalRecordNumber());
        dto.setAddress(patient.getAddress());
        return dto;
    }

    private Pageable createPageable(PageRequestDTO pageRequest) {
        Sort sort = Sort.by(pageRequest.getSortDirection().equalsIgnoreCase("DESC") ?
            Sort.Direction.DESC : Sort.Direction.ASC, pageRequest.getSortBy());
        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }
}