-- V2__insert_demo_users.sql
-- Usuarios de demostración que pueden iniciar sesión en el panel de administración.
-- Contraseña de los tres: demo123 (BCrypt cost 12, mismo encoder que PasswordConfig).
-- Los perfiles visibles en GET /api/users viven en user-service (V3__insert_demo_users.sql).

INSERT INTO users (username, email, password_hash, full_name, phone, is_active, is_verified)
VALUES
    ('admin@duoc.cl', 'admin@duoc.cl', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Administrador Demo', '+56911111111', true, true),
    ('vendedor@profesor.duoc.cl', 'vendedor@profesor.duoc.cl', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Vendedor Demo', '+56922222222', true, true),
    ('cliente@gmail.com', 'cliente@gmail.com', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Ana Demo', '+56933333333', true, true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@duoc.cl'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'VENDEDOR' FROM users WHERE email = 'vendedor@profesor.duoc.cl'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'CLIENTE' FROM users WHERE email = 'cliente@gmail.com'
ON CONFLICT DO NOTHING;
