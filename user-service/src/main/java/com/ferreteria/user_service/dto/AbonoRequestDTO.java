package com.ferreteria.user_service.dto;

import com.ferreteria.user_service.model.Abono;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class AbonoRequestDTO {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 12, fraction = 2, message = "Formato de monto inválido")
    private BigDecimal monto;

    @NotNull(message = "El tipo de abono es obligatorio")
    private Abono.TipoAbono tipo;

    @Size(max = 100, message = "La referencia externa no puede exceder 100 caracteres")
    private String referenciaExterna;

    @Size(max = 50, message = "El medio de pago no puede exceder 50 caracteres")
    private String medioPago;

    @NotNull(message = "La fecha de abono es obligatoria")
    private Instant fechaAbono;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}