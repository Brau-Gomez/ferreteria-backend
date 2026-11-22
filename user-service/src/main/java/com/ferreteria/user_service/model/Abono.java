package com.ferreteria.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "abonos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abono {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_corriente_id", nullable = false)
    private CuentaCorriente cuentaCorriente;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAbono tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAbono estado = EstadoAbono.PENDIENTE;

    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna; // N° de transferencia, comprobante, etc.

    @Column(name = "medio_pago", length = 50)
    private String medioPago; // TRANSFERENCIA, EFECTIVO, TARJETA, CHEQUE

    @Column(name = "fecha_abono", nullable = false)
    private Instant fechaAbono;

    @Column(name = "fecha_aplicacion")
    private Instant fechaAplicacion;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "procesado_por", length = 100)
    private String procesadoPor; // usuario/admin que validó el abono

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum TipoAbono {
        PAGO_CUOTA,       // pago de cuota mensual
        PAGO_TOTAL,       // pago total de deuda
        ABONO_EXTRAORDINARIO, // abono voluntario
        REEMBOLSO,        // devolución de dinero
        AJUSTE            // ajuste manual (intereses, comisiones, etc.)
    }

    public enum EstadoAbono {
        PENDIENTE,    // esperando validación
        APLICADO,     // abonado a la cuenta
        RECHAZADO,    // rechazado por admin
        ANULADO       // anulado posterior a aplicación
    }
}