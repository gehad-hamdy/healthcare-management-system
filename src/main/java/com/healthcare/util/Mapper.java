package com.healthcare.util;

import com.healthcare.dto.FacilityDTO;
import com.healthcare.dto.PatientDTO;
import com.healthcare.model.Facility;
import com.healthcare.model.Patient;

public class Mapper {
    public static FacilityDTO toFacilityDTO(Facility f, long patientCount) {
        return new FacilityDTO(f.getId(), f.getName(), f.getType(), f.getAddress(), f.isActive(), patientCount);
    }


    public static PatientDTO toPatientDTO(Patient p) {
        return new PatientDTO(p.getId(), p.getFacility() == null ? null : p.getFacility().getId(), p.getFullName(), p.getDob(), p.getGender(), p.getMedicalRecordNumber(), p.isActive());
    }
}