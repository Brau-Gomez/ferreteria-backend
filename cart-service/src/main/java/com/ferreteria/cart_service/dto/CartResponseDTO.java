package com.ferreteria.cart_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponseDTO {
    private List<CartItemResponseDTO> items;
    private BigDecimal total;
}
