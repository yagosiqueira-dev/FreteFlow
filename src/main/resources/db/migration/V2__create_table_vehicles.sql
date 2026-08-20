CREATE TABLE vehicles (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          license_plate VARCHAR(10) NOT NULL UNIQUE,
                          type VARCHAR(50) NOT NULL,
                          model VARCHAR(100) NOT NULL,
                          year INTEGER NOT NULL,
                          enabled BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);