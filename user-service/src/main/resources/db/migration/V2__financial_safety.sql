CREATE UNIQUE INDEX IF NOT EXISTS uk_abonos_referencia_externa
    ON abonos (referencia_externa)
    WHERE referencia_externa IS NOT NULL;

CREATE TABLE IF NOT EXISTS periodos_interes (
    id                    UUID PRIMARY KEY,
    cuenta_corriente_id   UUID           NOT NULL REFERENCES cuentas_corrientes (id) ON DELETE CASCADE,
    periodo               VARCHAR(7)     NOT NULL,
    monto                 NUMERIC(14, 2) NOT NULL,
    created_at            TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uk_periodo_interes_cuenta_periodo
        UNIQUE (cuenta_corriente_id, periodo)
);

CREATE INDEX IF NOT EXISTS idx_periodos_interes_cuenta
    ON periodos_interes (cuenta_corriente_id);
