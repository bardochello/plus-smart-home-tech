DROP TABLE IF EXISTS products CASCADE;

CREATE TABLE IF NOT EXISTS products (
                                        product_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR,
    description VARCHAR,
    image_src VARCHAR,
    quantity_state VARCHAR,
    product_state VARCHAR,
    rating FLOAT,
    category VARCHAR,
    price FLOAT
);