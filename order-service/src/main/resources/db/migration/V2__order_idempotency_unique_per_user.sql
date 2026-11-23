ALTER TABLE orders
    ALTER COLUMN shipping_commune_id TYPE VARCHAR(50)
    USING shipping_commune_id::text;

ALTER TABLE orders
    ALTER COLUMN shipping_region_id TYPE VARCHAR(50)
    USING shipping_region_id::text;

ALTER TABLE orders
    ADD CONSTRAINT uk_orders_user_idempotency
    UNIQUE (user_id, idempotency_key);
