package com.healthcare.service.impl;

import com.healthcare.dto.FacilityDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.dto.PatientDTO;
import com.healthcare.model.Facility;
import com.healthcare.model.Patient;
import com.healthcare.repository.FacilityRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.service.FacilityService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final PatientRepository patientRepository;

    @Override
    public Page<FacilityDTO> getAllFacilities(PageRequestDTO pageRequest, String search, Facility.FacilityType type) {
        Pageable pageable = createPageable(pageRequest);
        Page<Facility> facilities;

        if (search != null && !search.trim().isEmpty()) {
            facilities = facilityRepository.searchActiveFacilities(search, pageable);
        } else if (type != null) {
            facilities = facilityRepository.findByTypeAndIsActiveTrue(type, pageable);
        } else {
            facilities = facilityRepository.findByIsActiveTrue(pageable);
        }

        return facilities.map(this::convertToDTO);
    }

    @Override
    public FacilityDTO getFacilityById(Long id) {
        Facility facility = facilityRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Facility not found with id: " + id));
        return convertToDTO(facility);
    }

    @Transactional
    @Override
    public FacilityDTO createFacility(FacilityDTO facilityDTO) {
        Facility facility = Facility.builder()
            .name(facilityDTO.getName())
            .type(facilityDTO.getType())
            .address(facilityDTO.getAddress())
            .isActive(true)
            .build();

        Facility saved = facilityRepository.save(facility);
        return convertToDTO(saved);
    }

    @Transactional
    @Override
    public FacilityDTO updateFacility(Long id, FacilityDTO facilityDTO) {
        Facility facility = facilityRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Facility not found with id: " + id));

        facility.setName(facilityDTO.getName());
        facility.setType(facilityDTO.getType());
        facility.setAddress(facilityDTO.getAddress());

        Facility updated = facilityRepository.save(facility);
        return convertToDTO(updated);
    }

    @Transactional
    @Override
    public void softDeleteFacility(Long id) {
        Facility facility = facilityRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Facility not found with id: " + id));

        // Check if facility has patients
        Long patientCount = facilityRepository.countPatientsByFacilityId(id);
        if (patientCount > 0) {
            throw new IllegalStateException("Cannot delete facility with active patients. Reassign patients first.");
        }

        facility.setIsActive(false);
        facility.setDeletedAt(LocalDateTime.now());
        facilityRepository.save(facility);
    }

    @Override
    public Page<PatientDTO> getPatientsByFacility(Long facilityId, PageRequestDTO pageRequest, String search) {
        FacilityDTO facility = getFacilityById(facilityId); // Ensure facility exists

        Pageable pageable = createPageable(pageRequest);
        Page<Patient> patients;

        if (search != null && !search.trim().isEmpty()) {
            patients = patientRepository.searchPatientsByFacility(facilityId, search, pageable);
        } else {
            patients = patientRepository.findByFacilityIdAndDeletedAtIsNull(facilityId, pageable);
        }

        return patients.map(this::convertPatientToDTO);
    }

    private PatientDTO convertPatientToDTO(Patient patient) {
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

    private FacilityDTO convertToDTO(Facility facility) {
        FacilityDTO dto = new FacilityDTO();
        dto.setId(facility.getId());
        dto.setName(facility.getName());
        dto.setType(facility.getType());
        dto.setAddress(facility.getAddress());

        // Get patient count
        Long patientCount = facilityRepository.countPatientsByFacilityId(facility.getId());
        dto.setPatientCount(patientCount);

        return dto;
    }

    private Pageable createPageable(PageRequestDTO pageRequest) {
        Sort sort = Sort.by(pageRequest.getSortDirection().equalsIgnoreCase("DESC") ?
            Sort.Direction.DESC : Sort.Direction.ASC, pageRequest.getSortBy());
        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }
}