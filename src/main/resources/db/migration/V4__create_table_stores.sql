CREATE TABLE stores (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(100) NOT NULL,
                        origin VARCHAR(150) NOT NULL,
                        destination VARCHAR(150) NOT NULL,
                        default_value NUMERIC(10, 2) NOT NULL,
                        enabled BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);