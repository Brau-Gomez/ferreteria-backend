package com.ferreteria.product_service.dto;

import com.ferreteria.product_service.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponseDTO {

    private java.util.UUID id;
    private String url;
    private String altText;
    private int sortOrder;
    private boolean primary;
    private Instant createdAt;

    public static ProductImageResponseDTO fromEntity(ProductImage image) {
        return ProductImageResponseDTO.builder()
                .id(image.getId())
                .url(image.getUrl())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .primary(image.isPrimary())
                .createdAt(image.getCreatedAt())
                .build();
    }
}