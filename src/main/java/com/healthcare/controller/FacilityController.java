package com.healthcare.controller;

import com.healthcare.dto.FacilityDTO;
import com.healthcare.dto.PageRequestDTO;
import com.healthcare.model.Facility;
import com.healthcare.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping
    public ResponseEntity<Page<FacilityDTO>> getAllFacilities(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Facility.FacilityType type) {

        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSortBy(sortBy);
        pageRequest.setSortDirection(sortDirection);

        Page<FacilityDTO> facilities = facilityService.getAllFacilities(pageRequest, search, type);
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacilityDTO> getFacilityById(@PathVariable Long id) {
        FacilityDTO facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }

    @PostMapping
    public ResponseEntity<FacilityDTO> createFacility(@Valid @RequestBody FacilityDTO facilityDTO) {
        FacilityDTO created = facilityService.createFacility(facilityDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacilityDTO> updateFacility(@PathVariable Long id,
        @Valid @RequestBody FacilityDTO facilityDTO) {
        FacilityDTO updated = facilityService.updateFacility(id, facilityDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.softDeleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}