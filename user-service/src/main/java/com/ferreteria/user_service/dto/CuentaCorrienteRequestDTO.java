package com.ferreteria.user_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaCorrienteRequestDTO {

    @NotNull(message = "El límite de crédito es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El límite de crédito no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "Formato de límite de crédito inválido")
    private BigDecimal limiteCredito;

    @Min(value = 1, message = "El día de corte debe ser entre 1 y 28")
    @Max(value = 28, message = "El día de corte debe ser entre 1 y 28")
    private Integer diaCorte = 28;

    @Min(value = 1, message = "El día de vencimiento debe ser entre 1 y 30")
    @Max(value = 30, message = "El día de vencimiento debe ser entre 1 y 30")
    private Integer diaVencimiento = 7;

    @DecimalMin(value = "0.0", inclusive = true, message = "La tasa de interés no puede ser negativa")
    @DecimalMax(value = "1.0", inclusive = true, message = "La tasa de interés no puede exceder 100%")
    @Digits(integer = 1, fraction = 4, message = "Formato de tasa de interés inválido (ej: 0.015 para 1.5%)")
    private BigDecimal tasaInteresMensual = new BigDecimal("0.015");

    private Boolean requiereGarantia = false;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}