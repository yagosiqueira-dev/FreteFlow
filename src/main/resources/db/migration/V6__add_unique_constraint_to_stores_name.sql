ALTER TABLE stores
    ADD CONSTRAINT uq_stores_name UNIQUE (name);