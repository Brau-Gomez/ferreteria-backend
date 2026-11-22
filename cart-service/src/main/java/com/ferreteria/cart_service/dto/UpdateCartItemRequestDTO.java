package com.ferreteria.cart_service.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemRequestDTO {
    @Min(1)
    private int quantity;
}
