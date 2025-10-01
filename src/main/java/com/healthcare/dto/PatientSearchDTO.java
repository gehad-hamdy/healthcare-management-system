package com.healthcare.dto;

import lombok.Data;

@Data
public class PatientSearchDTO {
    private String firstName;
    private String lastName;
    private String email;
    private Long facilityId;
    private String medicalRecordNumber;
}