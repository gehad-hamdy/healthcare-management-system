package com.healthcare.service;

import com.healthcare.dto.FacilityDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.dto.PatientDTO;
import com.healthcare.model.Facility;
import org.springframework.data.domain.Page;

public interface FacilityService {

     Page<FacilityDTO> getAllFacilities(PageRequestDTO pageRequest, String search, Facility.FacilityType type);

     FacilityDTO getFacilityById(Long id);

     FacilityDTO createFacility(FacilityDTO facilityDTO);

     FacilityDTO updateFacility(Long id, FacilityDTO facilityDTO);

    void softDeleteFacility(Long id);

    Page<PatientDTO> getPatientsByFacility(Long facilityId, PageRequestDTO pageRequest, String search);
}