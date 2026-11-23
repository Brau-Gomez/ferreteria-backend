package com.ferreteria.cart_service.client;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private int stock;
    private int minStock;
    private boolean active;
    private boolean featured;
    private List<ProductImageDTO> images;
    private List<String> imageUrls;

    @Data
    public static class ProductImageDTO {
        private String url;
        private boolean primary;
    }
}
