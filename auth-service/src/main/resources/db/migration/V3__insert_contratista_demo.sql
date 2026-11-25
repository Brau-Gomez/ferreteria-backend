-- V3__insert_contratista_demo.sql
-- Contratista de demostración: cliente con cuenta corriente (la cuenta vive en user-service).
-- Contraseña: demo123 (mismo hash BCrypt que los demás usuarios demo).

INSERT INTO users (username, email, password_hash, full_name, phone, is_active, is_verified)
VALUES
    ('contratista@gmail.com', 'contratista@gmail.com', '$2a$12$rJjwj0P1GNIw/um2xMFP3.39FHC6JmwCa3EpZLBLT9a2ouRVg4hQy', 'Carlos Contratista', '+56944444444', true, true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'CONTRATISTA' FROM users WHERE email = 'contratista@gmail.com'
ON CONFLICT DO NOTHING;