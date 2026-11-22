-- V1__create_catalog_schema.sql
-- Esquema inicial para catalog-service

CREATE TABLE IF NOT EXISTS regions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_regions_code ON regions(code);
CREATE INDEX IF NOT EXISTS idx_regions_active ON regions(is_active);

CREATE TABLE IF NOT EXISTS communes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    region_id BIGINT NOT NULL REFERENCES regions(id) ON DELETE RESTRICT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_communes_code ON communes(code);
CREATE INDEX IF NOT EXISTS idx_communes_region ON communes(region_id);
CREATE INDEX IF NOT EXISTS idx_communes_active ON communes(is_active);

CREATE TABLE IF NOT EXISTS delivery_coverage (
    id BIGSERIAL PRIMARY KEY,
    comuna_id BIGINT NOT NULL UNIQUE REFERENCES communes(id) ON DELETE CASCADE,
    is_covered BOOLEAN NOT NULL DEFAULT false,
    delivery_cost NUMERIC(10,2),
    min_order_amount NUMERIC(10,2),
    estimated_days INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_coverage_comuna ON delivery_coverage(comuna_id);
CREATE INDEX IF NOT EXISTS idx_coverage_active ON delivery_coverage(is_active);

-- Datos semilla: Región de Coquimbo (código IV)
INSERT INTO regions (code, name, display_order, is_active) VALUES
('IV', 'Región de Coquimbo', 4, true)
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, display_order = EXCLUDED.display_order, is_active = EXCLUDED.is_active;

-- 15 comunas de la Región de Coquimbo
INSERT INTO communes (code, name, region_id, display_order, is_active) VALUES
('IV101', 'La Serena', (SELECT id FROM regions WHERE code = 'IV'), 1, true),
('IV102', 'Coquimbo', (SELECT id FROM regions WHERE code = 'IV'), 2, true),
('IV103', 'Andacollo', (SELECT id FROM regions WHERE code = 'IV'), 3, true),
('IV104', 'La Higuera', (SELECT id FROM regions WHERE code = 'IV'), 4, true),
('IV105', 'Paihuano', (SELECT id FROM regions WHERE code = 'IV'), 5, true),
('IV106', 'Vicuña', (SELECT id FROM regions WHERE code = 'IV'), 6, true),
('IV201', 'Ovalle', (SELECT id FROM regions WHERE code = 'IV'), 7, true),
('IV202', 'Combarbalá', (SELECT id FROM regions WHERE code = 'IV'), 8, true),
('IV203', 'Monte Patria', (SELECT id FROM regions WHERE code = 'IV'), 9, true),
('IV204', 'Punitaqui', (SELECT id FROM regions WHERE code = 'IV'), 10, true),
('IV205', 'Río Hurtado', (SELECT id FROM regions WHERE code = 'IV'), 11, true),
('IV301', 'Illapel', (SELECT id FROM regions WHERE code = 'IV'), 12, true),
('IV302', 'Canela', (SELECT id FROM regions WHERE code = 'IV'), 13, true),
('IV303', 'Los Vilos', (SELECT id FROM regions WHERE code = 'IV'), 14, true),
('IV304', 'Salamanca', (SELECT id FROM regions WHERE code = 'IV'), 15, true)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    region_id = EXCLUDED.region_id,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active;

-- Cobertura de despacho por defecto para las 15 comunas
INSERT INTO delivery_coverage (comuna_id, is_covered, delivery_cost, min_order_amount, estimated_days, is_active)
SELECT c.id, true,
    CASE
        WHEN c.code IN ('IV101', 'IV102') THEN 0.00      -- La Serena, Coquimbo: gratis
        WHEN c.code IN ('IV103', 'IV106', 'IV201', 'IV301') THEN 2500.00  -- principales: $2.500
        ELSE 5000.00                                     -- resto: $5.000
    END,
    CASE
        WHEN c.code IN ('IV101', 'IV102') THEN 0.00
        ELSE 20000.00
    END,
    CASE
        WHEN c.code IN ('IV101', 'IV102') THEN 1
        WHEN c.code IN ('IV103', 'IV106', 'IV201', 'IV301') THEN 2
        ELSE 3
    END,
    true
FROM communes c
JOIN regions r ON c.region_id = r.id
WHERE r.code = 'IV'
ON CONFLICT (comuna_id) DO UPDATE SET
    is_covered = EXCLUDED.is_covered,
    delivery_cost = EXCLUDED.delivery_cost,
    min_order_amount = EXCLUDED.min_order_amount,
    estimated_days = EXCLUDED.estimated_days,
    is_active = EXCLUDED.is_active;