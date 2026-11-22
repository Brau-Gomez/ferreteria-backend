package com.ferreteria.user_service.dto;

import com.ferreteria.user_service.model.Abono;
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
public class AbonoResponseDTO {

    private UUID id;
    private UUID cuentaCorrienteId;
    private BigDecimal monto;
    private Abono.TipoAbono tipo;
    private Abono.EstadoAbono estado;
    private String referenciaExterna;
    private String medioPago;
    private Instant fechaAbono;
    private Instant fechaAplicacion;
    private String observaciones;
    private String procesadoPor;
    private Instant createdAt;
    private Instant updatedAt;

    public static AbonoResponseDTO fromEntity(Abono abono) {
        return AbonoResponseDTO.builder()
                .id(abono.getId())
                .cuentaCorrienteId(abono.getCuentaCorriente() != null ? abono.getCuentaCorriente().getId() : null)
                .monto(abono.getMonto())
                .tipo(abono.getTipo())
                .estado(abono.getEstado())
                .referenciaExterna(abono.getReferenciaExterna())
                .medioPago(abono.getMedioPago())
                .fechaAbono(abono.getFechaAbono())
                .fechaAplicacion(abono.getFechaAplicacion())
                .observaciones(abono.getObservaciones())
                .procesadoPor(abono.getProcesadoPor())
                .createdAt(abono.getCreatedAt())
                .updatedAt(abono.getUpdatedAt())
                .build();
    }
}