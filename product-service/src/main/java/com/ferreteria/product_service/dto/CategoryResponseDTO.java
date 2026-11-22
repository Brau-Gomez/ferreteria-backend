package com.ferreteria.product_service.dto;

import com.ferreteria.product_service.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private java.util.UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private int sortOrder;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private int productCount;

    public static CategoryResponseDTO fromEntity(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }
}