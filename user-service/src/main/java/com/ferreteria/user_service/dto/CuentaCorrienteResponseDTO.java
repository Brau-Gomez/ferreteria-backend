package com.ferreteria.user_service.dto;

import com.ferreteria.user_service.model.CuentaCorriente;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaCorrienteResponseDTO {

    private UUID id;
    private UUID usuarioId;
    private BigDecimal limiteCredito;
    private BigDecimal saldoDisponible;
    private BigDecimal saldoUtilizado;
    private Integer diaCorte;
    private Integer diaVencimiento;
    private CuentaCorriente.EstadoCuenta estado;
    private BigDecimal tasaInteresMensual;
    private Boolean requiereGarantia;
    private String observaciones;
    private Instant createdAt;
    private Instant updatedAt;

    // Campos calculados
    private BigDecimal saldoTotal;
    private BigDecimal porcentajeUso;

    public static CuentaCorrienteResponseDTO fromEntity(CuentaCorriente cuenta) {
        return CuentaCorrienteResponseDTO.builder()
                .id(cuenta.getId())
                .usuarioId(cuenta.getUsuario() != null ? cuenta.getUsuario().getId() : null)
                .limiteCredito(cuenta.getLimiteCredito())
                .saldoDisponible(cuenta.getSaldoDisponible())
                .saldoUtilizado(cuenta.getSaldoUtilizado())
                .diaCorte(cuenta.getDiaCorte())
                .diaVencimiento(cuenta.getDiaVencimiento())
                .estado(cuenta.getEstado())
                .tasaInteresMensual(cuenta.getTasaInteresMensual())
                .requiereGarantia(cuenta.getRequiereGarantia())
                .observaciones(cuenta.getObservaciones())
                .createdAt(cuenta.getCreatedAt())
                .updatedAt(cuenta.getUpdatedAt())
                .saldoTotal(cuenta.getSaldoTotal())
                .porcentajeUso(cuenta.getPorcentajeUso())
                .build();
    }
}