package com.healthcare.mapper;

import com.healthcare.dto.FacilityDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.dto.PatientDTO;
import com.healthcare.model.Facility;
import com.healthcare.model.Patient;
import com.healthcare.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityMapper {

    private final FacilityRepository facilityRepository;

    public PatientDTO toPatientDTO(Patient patient) {
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

    public FacilityDTO toFacilityDTO(Facility facility) {
        FacilityDTO dto = new FacilityDTO();
        dto.setId(facility.getId());
        dto.setName(facility.getName());
        dto.setType(facility.getType());
        dto.setAddress(facility.getAddress());

        Long patientCount = facilityRepository.countPatientsByFacilityId(facility.getId());
        dto.setPatientCount(patientCount);

        return dto;
    }

    public Pageable createPageable(PageRequestDTO pageRequest) {
        Sort sort = Sort.by(pageRequest.getSortDirection().equalsIgnoreCase("DESC") ?
            Sort.Direction.DESC : Sort.Direction.ASC, pageRequest.getSortBy());
        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }
}