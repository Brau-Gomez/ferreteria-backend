package com.ferreteria.order_service.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class OrderRequestDTO {

    @NotNull(message = "Los items son obligatorios")
    @Size(min = 1, max = 100, message = "La orden debe tener entre 1 y 100 items")
    @Valid
    private List<OrderItemRequestDTO> items;

    @NotBlank(message = "La dirección de envío es obligatoria")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String shippingAddress;

    @NotBlank(message = "La comuna de envío es obligatoria")
    @Pattern(regexp = "\\d+", message = "El ID de comuna debe ser numérico")
    private String shippingCommuneId;

    @NotBlank(message = "La región de envío es obligatoria")
    @Pattern(regexp = "\\d+", message = "El ID de región debe ser numérico")
    private String shippingRegionId;

    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notes;

    @Data
    public static class OrderItemRequestDTO {

        @NotBlank(message = "El código del producto es obligatorio")
        @Size(max = 50, message = "El código no puede exceder 50 caracteres")
        private String productCode;

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Max(value = 100, message = "La cantidad no puede exceder 100")
        private int quantity;
    }
}