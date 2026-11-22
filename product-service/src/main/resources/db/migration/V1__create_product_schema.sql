-- Ferretería Los Maestros - Product Service Schema
-- V1: Creación inicial de tablas para productos, categorías e imágenes

-- Categorías
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE INDEX idx_categories_active ON categories (is_active);
CREATE INDEX idx_categories_sort_order ON categories (sort_order);

-- Productos
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL,
    compare_at_price NUMERIC(12, 2),
    cost_price NUMERIC(12, 2),
    stock INTEGER NOT NULL DEFAULT 0,
    min_stock INTEGER NOT NULL DEFAULT 0,
    max_stock INTEGER,
    weight_kg NUMERIC(8, 3),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_featured BOOLEAN NOT NULL DEFAULT false,
    category_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_product_code UNIQUE (code),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT
);

CREATE INDEX idx_products_active ON products (is_active);
CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_code ON products (code);
CREATE INDEX idx_products_featured ON products (is_featured) WHERE is_featured = true;
CREATE INDEX idx_products_stock_low ON products (stock) WHERE stock <= min_stock;

-- Imágenes de productos
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE INDEX idx_product_images_product ON product_images (product_id);
CREATE INDEX idx_product_images_primary ON product_images (product_id, is_primary) WHERE is_primary = true;

-- Datos semilla: categorías básicas
INSERT INTO categories (name, description, sort_order) VALUES
    ('Herramientas Manuales', 'Martillos, destornilladores, llaves, alicates y más', 1),
    ('Herramientas Eléctricas', 'Taladros, sierras, lijadoras, atornilladores', 2),
    ('Ferretería General', 'Tornillos, clavos, bisagras, cerraduras, cadena', 3),
    ('Pintura y Acabados', 'Pinturas, barnices, brochas, rodillos, cintas', 4),
    ('Plomería', 'Tuberías, grifos, válvulas, conexiones, sanitarios', 5),
    ('Electricidad', 'Cables, interruptores, tomacorrientes, breakers', 6),
    ('Seguridad', 'Candados, cámaras, alarmas, extintores, señalización', 7),
    ('Jardinería', 'Mangueras, macetas, tijeras, rastrillos, fertilizantes', 8),
    ('Organización', 'Cajas, estantes, ganchos, carros, maletines', 9),
    ('Equipos de Protección', 'Cascos, guantes, lentes, botas, arneses', 10);