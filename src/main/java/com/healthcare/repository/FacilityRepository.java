package com.healthcare.repository;

import com.healthcare.model.Facility;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    Page<Facility> findByIsActiveTrue(Pageable pageable);

    Page<Facility> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    Page<Facility> findByTypeAndIsActiveTrue(Facility.FacilityType type, Pageable pageable);

    @Query("SELECT f FROM Facility f WHERE f.isActive = true AND (" +
        "LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(f.type) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Facility> searchActiveFacilities(String search, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.facility.id = :facilityId AND p.deletedAt IS NULL")
    Long countPatientsByFacilityId(Long facilityId);

    Optional<Facility> findByIdAndIsActiveTrue(Long id);
}