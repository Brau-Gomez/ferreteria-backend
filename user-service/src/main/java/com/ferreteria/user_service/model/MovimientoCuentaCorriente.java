package com.ferreteria.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimientos_cuenta_corriente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoCuentaCorriente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_corriente_id", nullable = false)
    private CuentaCorriente cuentaCorriente;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto; // positivo = cargo, negativo = abono

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "referencia_id") // ID de orden, factura, abono, etc.
    private UUID referenciaId;

    @Column(name = "referencia_tipo", length = 50) // ORDEN, FACTURA, ABONO, AJUSTE
    private String referenciaTipo;

    @Column(name = "saldo_anterior", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_nuevo", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoNuevo;

    @Column(name = "saldo_disponible_anterior", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoDisponibleAnterior;

    @Column(name = "saldo_disponible_nuevo", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoDisponibleNuevo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum TipoMovimiento {
        CARGO_COMPRA,       // compra con cuenta corriente
        CARGO_INTERES,      // interés mensual
        CARGO_COMISION,     // comisión por mora/administración
        ABONO_PAGO,         // pago de cliente
        ABONO_REEMBOLSO,    // reembolso
        AJUSTE_MANUAL,      // ajuste administrativo
        APERTURA_CREDITO,   // aumento de límite
        REDUCCION_CREDITO   // disminución de límite
    }
}