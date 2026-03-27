DROP TABLE IF EXISTS warehouse_products CASCADE;

CREATE TABLE IF NOT EXISTS warehouse_products (
                                                  product_id VARCHAR PRIMARY KEY,
                                                  fragile BOOLEAN NOT NULL,
                                                  dimension_width DOUBLE PRECISION NOT NULL,
                                                  dimension_height DOUBLE PRECISION NOT NULL,
                                                  dimension_depth DOUBLE PRECISION NOT NULL,
                                                  weight DOUBLE PRECISION NOT NULL,
                                                  quantity BIGINT NOT NULL
);