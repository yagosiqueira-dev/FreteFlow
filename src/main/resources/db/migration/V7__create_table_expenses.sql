CREATE TABLE expenses (
                          id UUID PRIMARY KEY,
                          freight_id UUID NOT NULL,
                          description VARCHAR(255) NOT NULL,
                          amount NUMERIC(15, 2) NOT NULL,
                          expense_date DATE NOT NULL,
                          created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                          CONSTRAINT fk_expense_freight FOREIGN KEY (freight_id) REFERENCES freights(id) ON DELETE CASCADE
);