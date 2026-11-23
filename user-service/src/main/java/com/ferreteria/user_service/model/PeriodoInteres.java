package com.ferreteria.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "periodos_interes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_periodo_interes_cuenta_periodo",
                columnNames = {"cuenta_corriente_id", "periodo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoInteres {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_corriente_id", nullable = false)
    private CuentaCorriente cuentaCorriente;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
