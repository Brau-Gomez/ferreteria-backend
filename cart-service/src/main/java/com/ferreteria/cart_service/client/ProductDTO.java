package com.ferreteria.cart_service.client;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private String code;
    private BigDecimal price;
    private int stock;
    private boolean active;
}
