-- Ferretería Los Maestros - Order Service Schema
-- V1: Creación inicial de tablas para órdenes, items, despachos y pagos

-- Órdenes
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(12, 2) NOT NULL,
    tax NUMERIC(12, 2) NOT NULL,
    shipping_cost NUMERIC(12, 2) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    shipping_address TEXT,
    shipping_commune_id UUID,
    shipping_region_id UUID,
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    notes TEXT,
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);
CREATE INDEX idx_orders_idempotency_key ON orders (idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Items de orden
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    total_price NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- Despachos
CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    shipping_address TEXT,
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_shipments_order_id ON shipments (order_id);
CREATE INDEX idx_shipments_status ON shipments (status);
CREATE INDEX idx_shipments_tracking ON shipments (tracking_number) WHERE tracking_number IS NOT NULL;

-- Pagos
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(30) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    transaction_id VARCHAR(100),
    gateway_response TEXT,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_transaction ON payments (transaction_id) WHERE transaction_id IS NOT NULL;

-- Tabla de claves de idempotencia para validación rápida
CREATE TABLE idempotency_keys (
    key_hash VARCHAR(64) PRIMARY KEY,
    order_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_idempotency_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_idempotency_order ON idempotency_keys (order_id);