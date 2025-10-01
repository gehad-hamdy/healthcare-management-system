package com.healthcare.dto;

import com.healthcare.model.Facility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FacilityDTO {
    private Long id;

    @NotBlank(message = "Facility name is required")
    private String name;

    @NotNull(message = "Facility type is required")
    private Facility.FacilityType type;

    @NotBlank(message = "Address is required")
    private String address;

    private Long patientCount;
}