package com.ferreteria.user_service.dto;

import com.ferreteria.user_service.model.MovimientoCuentaCorriente;
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
public class MovimientoCuentaCorrienteResponseDTO {

    private UUID id;
    private UUID cuentaCorrienteId;
    private BigDecimal monto;
    private MovimientoCuentaCorriente.TipoMovimiento tipo;
    private String descripcion;
    private UUID referenciaId;
    private String referenciaTipo;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoNuevo;
    private BigDecimal saldoDisponibleAnterior;
    private BigDecimal saldoDisponibleNuevo;
    private Instant createdAt;

    public static MovimientoCuentaCorrienteResponseDTO fromEntity(MovimientoCuentaCorriente movimiento) {
        return MovimientoCuentaCorrienteResponseDTO.builder()
                .id(movimiento.getId())
                .cuentaCorrienteId(movimiento.getCuentaCorriente() != null ? movimiento.getCuentaCorriente().getId() : null)
                .monto(movimiento.getMonto())
                .tipo(movimiento.getTipo())
                .descripcion(movimiento.getDescripcion())
                .referenciaId(movimiento.getReferenciaId())
                .referenciaTipo(movimiento.getReferenciaTipo())
                .saldoAnterior(movimiento.getSaldoAnterior())
                .saldoNuevo(movimiento.getSaldoNuevo())
                .saldoDisponibleAnterior(movimiento.getSaldoDisponibleAnterior())
                .saldoDisponibleNuevo(movimiento.getSaldoDisponibleNuevo())
                .createdAt(movimiento.getCreatedAt())
                .build();
    }
}