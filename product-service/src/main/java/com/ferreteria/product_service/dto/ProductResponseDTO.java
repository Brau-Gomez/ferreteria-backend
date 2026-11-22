package com.ferreteria.product_service.dto;

import com.ferreteria.product_service.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private java.util.UUID id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal costPrice;
    private int stock;
    private int minStock;
    private Integer maxStock;
    private BigDecimal weightKg;
    private boolean active;
    private boolean featured;
    private CategoryResponseDTO category;
    private List<ProductImageResponseDTO> images;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponseDTO fromEntity(Product product) {
        ProductResponseDTO dto = ProductResponseDTO.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .costPrice(product.getCostPrice())
                .stock(product.getStock())
                .minStock(product.getMinStock())
                .maxStock(product.getMaxStock())
                .weightKg(product.getWeightKg())
                .active(product.isActive())
                .featured(product.isFeatured())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        if (product.getCategory() != null) {
            dto.setCategory(CategoryResponseDTO.fromEntity(product.getCategory()));
        }
        if (product.getImages() != null) {
            dto.setImages(product.getImages().stream()
                    .map(ProductImageResponseDTO::fromEntity)
                    .toList());
        }
        return dto;
    }

    // Versión ligera para listados (sin imágenes ni categoría completa)
    public static ProductResponseDTO fromEntityLite(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .stock(product.getStock())
                .active(product.isActive())
                .featured(product.isFeatured())
                .category(product.getCategory() != null ? CategoryResponseDTO.fromEntity(product.getCategory()) : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}