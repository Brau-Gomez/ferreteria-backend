package com.ferreteria.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cuentas_corrientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaCorriente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "limite_credito", nullable = false, precision = 14, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "saldo_disponible", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoDisponible = BigDecimal.ZERO;

    @Column(name = "saldo_utilizado", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoUtilizado = BigDecimal.ZERO;

    @Column(name = "dia_corte", nullable = false)
    private Integer diaCorte = 28; // día del mes para corte de estado de cuenta

    @Column(name = "dia_vencimiento", nullable = false)
    private Integer diaVencimiento = 7; // días después del corte para pago

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    @Column(name = "tasa_interes_mensual", precision = 5, scale = 4)
    private BigDecimal tasaInteresMensual = new BigDecimal("0.015"); // 1.5% default

    @Column(name = "requiere_garantia", nullable = false)
    private Boolean requiereGarantia = false;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @OneToMany(mappedBy = "cuentaCorriente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Abono> abonos = new HashSet<>();

    @OneToMany(mappedBy = "cuentaCorriente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MovimientoCuentaCorriente> movimientos = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum EstadoCuenta {
        ACTIVA, SUSPENDIDA, CERRADA, EN_MORA, BLOQUEADA
    }

    // Métodos de conveniencia
    public BigDecimal getSaldoTotal() {
        return saldoDisponible.add(saldoUtilizado);
    }

    public BigDecimal getPorcentajeUso() {
        if (limiteCredito.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return saldoUtilizado.divide(limiteCredito, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public boolean tieneCupoDisponible(BigDecimal monto) {
        return saldoDisponible.compareTo(monto) >= 0;
    }
}