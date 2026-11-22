package com.ferreteria.user_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DireccionRequestDTO {

    @NotBlank(message = "El alias es obligatorio")
    @Size(max = 50, message = "El alias no puede exceder 50 caracteres")
    private String alias;

    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 200, message = "La calle no puede exceder 200 caracteres")
    private String calle;

    @NotBlank(message = "El número es obligatorio")
    @Size(max = 20, message = "El número no puede exceder 20 caracteres")
    private String numero;

    @Size(max = 20, message = "El departamento no puede exceder 20 caracteres")
    private String departamento;

    @NotBlank(message = "La comuna es obligatoria")
    @Size(max = 100, message = "La comuna no puede exceder 100 caracteres")
    private String comuna;

    @NotBlank(message = "La región es obligatoria")
    @Size(max = 100, message = "La región no puede exceder 100 caracteres")
    private String region;

    @Size(max = 10, message = "El código postal no puede exceder 10 caracteres")
    private String codigoPostal;

    @Size(max = 500, message = "La referencia no puede exceder 500 caracteres")
    private String referencia;

    @DecimalMin(value = "-90.0", message = "Latitud inválida")
    @DecimalMax(value = "90.0", message = "Latitud inválida")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "Longitud inválida")
    @DecimalMax(value = "180.0", message = "Longitud inválida")
    private Double longitud;

    private Boolean esPrincipal = false;
    private Boolean esFacturacion = false;
    private Boolean esDespacho = false;
}