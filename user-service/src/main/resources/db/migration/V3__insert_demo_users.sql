-- V3__insert_demo_users.sql
-- Perfiles de demostración que lista el panel de administración (GET /api/users).
-- El login lo resuelve auth-service (V2__insert_demo_users.sql); aquí solo vive el perfil.
-- password_hash es obligatorio por el esquema, pero este servicio nunca autentica:
-- se usa el mismo hash de demo123 para que no queden valores inconsistentes.
-- Los RUT van sin puntos ni guion porque el frontend compara contra normalizarRun().

INSERT INTO usuarios (id, email, password_hash, nombre_completo, telefono, rut, rol, estado, email_verificado, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin@duoc.cl', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Administrador Demo', '+56911111111', '123456785', 'ADMIN', 'ACTIVO', true, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'vendedor@profesor.duoc.cl', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Vendedor Demo', '+56922222222', '111111111', 'VENDEDOR', 'ACTIVO', true, NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', 'cliente@gmail.com', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Ana Demo', '+56933333333', '222222222', 'CLIENTE', 'ACTIVO', true, NOW(), NOW()),
    ('44444444-4444-4444-4444-444444444444', 'contratista@gmail.com', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Carlos Contratista', '+56944444444', '333333333', 'CONTRATISTA', 'ACTIVO', true, NOW(), NOW()),
    ('55555555-5555-5555-5555-555555555555', 'maria.soto@gmail.com', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'María Soto', '+56955555555', '176543213', 'CLIENTE', 'ACTIVO', true, NOW(), NOW()),
    ('66666666-6666-6666-6666-666666666666', 'pedro.ramirez@gmail.com', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Pedro Ramírez', '+56966666666', '987654325', 'CLIENTE', 'SUSPENDIDO', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Cuenta corriente del contratista, para que su ficha no aparezca vacía.
INSERT INTO cuentas_corrientes (id, usuario_id, limite_credito, saldo_disponible, saldo_utilizado,
                                dia_corte, dia_vencimiento, estado, tasa_interes_mensual,
                                requiere_garantia, created_at, updated_at)
SELECT '77777777-7777-7777-7777-777777777777', id, 500000.00, 500000.00, 0.00,
       28, 7, 'ACTIVA', 0.0150, false, NOW(), NOW()
FROM usuarios
WHERE email = 'contratista@gmail.com'
ON CONFLICT (usuario_id) DO NOTHING;
