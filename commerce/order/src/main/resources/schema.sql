DROP TABLE IF EXISTS order_products CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

CREATE TABLE IF NOT EXISTS orders (
                                      order_id UUID PRIMARY KEY,
                                      shopping_cart_id UUID,
                                      state VARCHAR NOT NULL,
                                      payment_id UUID,
                                      delivery_id UUID,
                                      delivery_weight DOUBLE PRECISION,
                                      delivery_volume DOUBLE PRECISION,
                                      fragile BOOLEAN,
                                      total_price DOUBLE PRECISION,
                                      delivery_price DOUBLE PRECISION,
                                      product_price DOUBLE PRECISION,
                                      username VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS order_products (
                                              order_id UUID NOT NULL,
                                              product_id VARCHAR NOT NULL,
                                              quantity BIGINT NOT NULL,
                                              PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
    );