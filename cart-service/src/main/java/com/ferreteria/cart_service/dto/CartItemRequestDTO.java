package com.ferreteria.cart_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CartItemRequestDTO {
    @NotBlank
    private String code;

    @Min(1)
    private int quantity;
}
