package com.healthcare.controller;

import com.healthcare.dto.PatientDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.dto.PatientSearchDTO;
import com.healthcare.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<Page<PatientDTO>> getAllPatients(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection,
        @RequestParam(required = false) String search) {

        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSortBy(sortBy);
        pageRequest.setSortDirection(sortDirection);

        Page<PatientDTO> patients = patientService.getAllPatients(pageRequest, search);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO createdPatient = patientService.createPatient(patientDTO);
        return ResponseEntity.ok(createdPatient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id,
        @Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO updatedPatient = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(updatedPatient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.softDeletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<Page<PatientDTO>> getPatientsByFacility(
        @PathVariable Long facilityId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection,
        @RequestParam(required = false) String search) {

        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSortBy(sortBy);
        pageRequest.setSortDirection(sortDirection);

        Page<PatientDTO> patients = patientService.getPatientsByFacility(facilityId, pageRequest, search);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PatientDTO>> searchPatients(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) Long facilityId,
        @RequestParam(required = false) String medicalRecordNumber) {

        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(page);
        pageRequest.setSize(size);

        PatientSearchDTO searchDTO = new PatientSearchDTO();
        searchDTO.setFirstName(firstName);
        searchDTO.setLastName(lastName);
        searchDTO.setEmail(email);
        searchDTO.setFacilityId(facilityId);
        searchDTO.setMedicalRecordNumber(medicalRecordNumber);

        Page<PatientDTO> patients = patientService.searchPatients(pageRequest, searchDTO);
        return ResponseEntity.ok(patients);
    }
}