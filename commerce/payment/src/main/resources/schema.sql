DROP TABLE IF EXISTS payments CASCADE;
CREATE TABLE IF NOT EXISTS payments (
                                        payment_id UUID PRIMARY KEY,
                                        order_id UUID NOT NULL,
                                        product_price DOUBLE PRECISION NOT NULL,
                                        delivery_price DOUBLE PRECISION NOT NULL,
                                        fee DOUBLE PRECISION NOT NULL,
                                        total DOUBLE PRECISION NOT NULL,
                                        status VARCHAR NOT NULL
);