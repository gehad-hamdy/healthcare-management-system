package com.healthcare.service.impl;

import com.healthcare.dto.FacilityDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.dto.PatientDTO;
import com.healthcare.mapper.EntityMapper;
import com.healthcare.model.Facility;
import com.healthcare.model.Patient;
import com.healthcare.repository.FacilityRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.service.FacilityService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final PatientRepository patientRepository;
    private final EntityMapper facilityMapper;

    @Override
    public Page<FacilityDTO> getAllFacilities(final PageRequestDTO pageRequest, final String search, final Facility.FacilityType type) {
        final Pageable pageable = facilityMapper.createPageable(pageRequest);
        Page<Facility> facilities;

        if (search != null && !search.trim().isEmpty()) {
            facilities = facilityRepository.searchActiveFacilities(search, pageable);
        } else if (type != null) {
            facilities = facilityRepository.findByTypeAndIsActiveTrue(type, pageable);
        } else {
            facilities = facilityRepository.findByIsActiveTrue(pageable);
        }

        return facilities.map(facilityMapper::toFacilityDTO);
    }

    @Override
    public FacilityDTO getFacilityById(final Long id) {
        final Facility facility = facilityRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Facility not found with id: " + id));

        return facilityMapper.toFacilityDTO(facility);
    }

    @Transactional
    @Override
    public FacilityDTO createFacility(final FacilityDTO facilityDTO) {
        final Facility facility = Facility.builder()
            .name(facilityDTO.getName())
            .type(facilityDTO.getType())
            .address(facilityDTO.getAddress())
            .isActive(true)
            .build();

        final Facility saved = facilityRepository.save(facility);

        return facilityMapper.toFacilityDTO(saved);
    }

    @Transactional
    @Override
    public FacilityDTO updateFacility(final Long id, final FacilityDTO facilityDTO) {
        final Facility facility = facilityRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Facility not found with id: " + id));

        facility.setName(facilityDTO.getName());
        facility.setType(facilityDTO.getType());
        facility.setAddress(facilityDTO.getAddress());

        final Facility updated = facilityRepository.save(facility);

        return facilityMapper.toFacilityDTO(updated);
    }

    @Transactional
    @Override
    public void softDeleteFacility(final Long id) {
        final Facility facility = facilityRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Facility not found with id: " + id));

        final Long patientCount = facilityRepository.countPatientsByFacilityId(id);
        if (patientCount > 0) {
            throw new IllegalStateException("Cannot delete facility with active patients. Reassign patients first.");
        }

        facility.setIsActive(false);
        facility.setDeletedAt(LocalDateTime.now());

        facilityRepository.save(facility);
    }

    @Override
    public Page<PatientDTO> getPatientsByFacility(final Long facilityId, final PageRequestDTO pageRequest, final String search) {
        final FacilityDTO facility = getFacilityById(facilityId); // Ensure facility exists

        final Pageable pageable = facilityMapper.createPageable(pageRequest);

        Page<Patient> patients;

        if (search != null && !search.trim().isEmpty()) {
            patients = patientRepository.searchPatientsByFacility(facilityId, search, pageable);
        } else {
            patients = patientRepository.findByFacilityIdAndDeletedAtIsNull(facilityId, pageable);
        }

        return patients.map(facilityMapper::toPatientDTO);
    }
}