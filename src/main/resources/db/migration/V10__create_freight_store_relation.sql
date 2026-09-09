CREATE TABLE tb_freight_store (
                                  freight_id UUID NOT NULL,
                                  store_id UUID NOT NULL,
                                  PRIMARY KEY (freight_id, store_id),
                                  FOREIGN KEY (freight_id) REFERENCES freights (id) ON DELETE CASCADE,
                                  FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);

INSERT INTO tb_freight_store (freight_id, store_id)
SELECT id, store_id FROM freights WHERE store_id IS NOT NULL;

ALTER TABLE freights DROP COLUMN store_id;