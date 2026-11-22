package com.ferreteria.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductImageRequestDTO {

    @NotBlank(message = "La URL es obligatoria")
    @Size(max = 500, message = "La URL no puede exceder 500 caracteres")
    private String url;

    @Size(max = 200, message = "El texto alternativo no puede exceder 200 caracteres")
    private String altText;

    private Integer sortOrder;

    private Boolean primary = false;
}