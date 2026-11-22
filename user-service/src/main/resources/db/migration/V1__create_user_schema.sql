-- V1__create_user_schema.sql
-- Esquema inicial para user-service. Los tipos deben coincidir con las entidades JPA
-- porque spring.jpa.hibernate.ddl-auto=validate falla al arrancar si no coinciden:
--   UUID + GenerationType.UUID      -> uuid
--   Instant (@CreationTimestamp)    -> timestamp with time zone (TIMESTAMPTZ)
--   BigDecimal(precision, scale)    -> numeric(p,s)
--   EnumType.STRING + length=20     -> varchar(20)

CREATE TABLE IF NOT EXISTS usuarios (
    id               UUID PRIMARY KEY,
    email            VARCHAR(100) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    nombre_completo  VARCHAR(150) NOT NULL,
    telefono         VARCHAR(20)  NOT NULL,
    rut              VARCHAR(12)  UNIQUE,
    rol              VARCHAR(20)  NOT NULL,
    estado           VARCHAR(20)  NOT NULL,
    email_verificado BOOLEAN      NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios (email);
CREATE INDEX IF NOT EXISTS idx_usuarios_rut ON usuarios (rut);

CREATE TABLE IF NOT EXISTS direcciones (
    id             UUID PRIMARY KEY,
    usuario_id     UUID         NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    alias          VARCHAR(50)  NOT NULL,
    calle          VARCHAR(200) NOT NULL,
    numero         VARCHAR(20)  NOT NULL,
    departamento   VARCHAR(20),
    comuna         VARCHAR(100) NOT NULL,
    region         VARCHAR(100) NOT NULL,
    codigo_postal  VARCHAR(10),
    referencia     VARCHAR(500),
    latitud        DOUBLE PRECISION,
    longitud       DOUBLE PRECISION,
    es_principal   BOOLEAN      NOT NULL,
    es_facturacion BOOLEAN      NOT NULL,
    es_despacho    BOOLEAN      NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_direcciones_usuario ON direcciones (usuario_id);

CREATE TABLE IF NOT EXISTS cuentas_corrientes (
    id                    UUID PRIMARY KEY,
    usuario_id            UUID           NOT NULL UNIQUE REFERENCES usuarios (id) ON DELETE CASCADE,
    limite_credito        NUMERIC(14, 2) NOT NULL,
    saldo_disponible      NUMERIC(14, 2) NOT NULL,
    saldo_utilizado       NUMERIC(14, 2) NOT NULL,
    dia_corte             INTEGER        NOT NULL,
    dia_vencimiento       INTEGER        NOT NULL,
    estado                VARCHAR(20)    NOT NULL,
    tasa_interes_mensual  NUMERIC(5, 4),
    requiere_garantia     BOOLEAN        NOT NULL,
    observaciones         VARCHAR(1000),
    created_at            TIMESTAMPTZ    NOT NULL,
    updated_at            TIMESTAMPTZ    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cuentas_corrientes_usuario ON cuentas_corrientes (usuario_id);

CREATE TABLE IF NOT EXISTS movimientos_cuenta_corriente (
    id                        UUID PRIMARY KEY,
    cuenta_corriente_id       UUID           NOT NULL REFERENCES cuentas_corrientes (id) ON DELETE CASCADE,
    monto                     NUMERIC(14, 2) NOT NULL,
    tipo                      VARCHAR(20)    NOT NULL,
    descripcion               VARCHAR(500)   NOT NULL,
    referencia_id             UUID,
    referencia_tipo           VARCHAR(50),
    saldo_anterior            NUMERIC(14, 2) NOT NULL,
    saldo_nuevo               NUMERIC(14, 2) NOT NULL,
    saldo_disponible_anterior NUMERIC(14, 2) NOT NULL,
    saldo_disponible_nuevo    NUMERIC(14, 2) NOT NULL,
    created_at                TIMESTAMPTZ    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_movimientos_cuenta ON movimientos_cuenta_corriente (cuenta_corriente_id);

CREATE TABLE IF NOT EXISTS abonos (
    id                  UUID PRIMARY KEY,
    cuenta_corriente_id UUID           NOT NULL REFERENCES cuentas_corrientes (id) ON DELETE CASCADE,
    monto               NUMERIC(14, 2) NOT NULL,
    tipo                VARCHAR(20)    NOT NULL,
    estado              VARCHAR(20)    NOT NULL,
    referencia_externa  VARCHAR(100),
    medio_pago          VARCHAR(50),
    fecha_abono         TIMESTAMPTZ    NOT NULL,
    fecha_aplicacion    TIMESTAMPTZ,
    observaciones       VARCHAR(500),
    procesado_por       VARCHAR(100),
    created_at          TIMESTAMPTZ    NOT NULL,
    updated_at          TIMESTAMPTZ    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_abonos_cuenta ON abonos (cuenta_corriente_id);
