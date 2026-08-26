ALTER TABLE expenses DROP CONSTRAINT fk_expense_freight;

ALTER TABLE expenses RENAME COLUMN freight_id TO vehicle_id;

ALTER TABLE expenses ADD CONSTRAINT fk_expense_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE;