CREATE TABLE facilities (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            type VARCHAR(100) NOT NULL CHECK (type IN ('HOSPITAL', 'CLINIC', 'LAB', 'PHARMACY', 'OTHER')),
                            address JSONB NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMP NULL,
                            is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE patients (
                          id BIGSERIAL PRIMARY KEY,
                          facility_id BIGINT NOT NULL,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          email VARCHAR(255),
                          phone VARCHAR(50),
                          date_of_birth DATE NOT NULL,
                          gender VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNSPECIFIED')),
                          medical_record_number VARCHAR(100) UNIQUE,
                          address JSONB,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL,
                          FOREIGN KEY (facility_id) REFERENCES facilities(id)
);

CREATE INDEX idx_facilities_name ON facilities(name);
CREATE INDEX idx_facilities_type ON facilities(type);
CREATE INDEX idx_facilities_active ON facilities(is_active) WHERE is_active = true;
CREATE INDEX idx_patients_facility_id ON patients(facility_id);
CREATE INDEX idx_patients_name ON patients(first_name, last_name);
CREATE INDEX idx_patients_email ON patients(email);
CREATE INDEX idx_patients_mrn ON patients(medical_record_number);
CREATE INDEX idx_patients_active ON patients(deleted_at) WHERE deleted_at IS NULL;