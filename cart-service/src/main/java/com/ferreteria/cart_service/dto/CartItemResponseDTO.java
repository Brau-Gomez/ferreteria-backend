package com.ferreteria.cart_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponseDTO {
    private String code;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
