package com.healthcare.service;

import com.healthcare.dto.FacilityDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.model.Facility;
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

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final PatientRepository patientRepository;

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

    public FacilityDTO getFacilityById(Long id) {
        Facility facility = facilityRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Facility not found with id: " + id));
        return convertToDTO(facility);
    }

    @Transactional
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