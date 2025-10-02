package com.healthcare.repository;

import com.healthcare.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    Page<Patient> findByDeletedAtIsNull(Pageable pageable);

    Page<Patient> findByFacilityIdAndDeletedAtIsNull(Long facilityId, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.deletedAt IS NULL AND (" +
        "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(p.medicalRecordNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.deletedAt IS NULL AND " +
        "p.facility.id = :facilityId AND (" +
        "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Patient> searchPatientsByFacility(@Param("facilityId") Long facilityId,
        @Param("search") String search, Pageable pageable);

    Optional<Patient> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByMedicalRecordNumberAndDeletedAtIsNull(String medicalRecordNumber);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.deletedAt IS NULL")
    long countActivePatients();
}