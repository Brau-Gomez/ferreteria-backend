package com.ferreteria.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String name;

    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de comparación no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio de comparación debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal compareAtPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El costo debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal costPrice;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minStock = 0;

    @Min(value = 0, message = "El stock máximo no puede ser negativo")
    private Integer maxStock;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso no puede ser negativo")
    @Digits(integer = 5, fraction = 3, message = "El peso debe tener máximo 5 dígitos enteros y 3 decimales")
    private BigDecimal weightKg;

    @NotNull(message = "La categoría es obligatoria")
    private java.util.UUID categoryId;

    private Boolean active = true;

    private Boolean featured = false;
}