CREATE TABLE freights (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          driver_id UUID NOT NULL,
                          vehicle_id UUID NOT NULL,
                          store_id UUID NOT NULL,
                          freight_value NUMERIC(10, 2) NOT NULL,
                          freight_date TIMESTAMP NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_freight_driver FOREIGN KEY (driver_id) REFERENCES drivers (id),
                          CONSTRAINT fk_freight_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
                          CONSTRAINT fk_freight_store FOREIGN KEY (store_id) REFERENCES stores (id)
);