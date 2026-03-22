DROP TABLE IF EXISTS reserved_products CASCADE;
DROP TABLE IF EXISTS warehouse_products CASCADE;
DROP TABLE IF EXISTS dimensions CASCADE;

CREATE TABLE IF NOT EXISTS dimensions (
                                          dimension_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    depth DOUBLE PRECISION NOT NULL
    );

CREATE TABLE IF NOT EXISTS warehouse_products (
                                                  warehouse_product_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    product_id VARCHAR NOT NULL UNIQUE,
    fragile BOOLEAN NOT NULL,
    dimension_id UUID NOT NULL REFERENCES dimensions(dimension_id) ON DELETE CASCADE,
    weight DOUBLE PRECISION NOT NULL,
    quantity BIGINT NOT NULL
    );

CREATE TABLE IF NOT EXISTS reserved_products (
                                                 reserved_products_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    shopping_cart_id UUID NOT NULL,
    product_id VARCHAR NOT NULL REFERENCES warehouse_products(product_id) ON DELETE CASCADE,
    reserved_quantity BIGINT NOT NULL
    );