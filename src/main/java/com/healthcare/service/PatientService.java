package com.healthcare.service;

import com.healthcare.dto.PageRequestDTO;
import com.healthcare.dto.PatientDTO;
import com.healthcare.dto.PatientSearchDTO;
import org.springframework.data.domain.Page;

public interface PatientService {
    Page<PatientDTO> getAllPatients(PageRequestDTO pageRequest, String search);
    PatientDTO getPatientById(Long id);
    PatientDTO createPatient(PatientDTO patientDTO);
    PatientDTO updatePatient(Long id, PatientDTO patientDTO);
    void softDeletePatient(Long id);
    Page<PatientDTO> getPatientsByFacility(Long facilityId, PageRequestDTO pageRequest, String search);
    Page<PatientDTO> searchPatients(PageRequestDTO pageRequest, PatientSearchDTO searchDTO);
}
