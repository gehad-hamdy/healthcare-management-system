package com.healthcare.dto;

import com.healthcare.model.Patient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PatientDTO {
    private Long id;

    @NotNull(message = "Facility ID is required")
    private Long facilityId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String email;
    private String phone;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    private Patient.Gender gender;
    private String medicalRecordNumber;
    private String address;
}