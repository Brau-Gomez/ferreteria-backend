package com.ferreteria.catalog_service.dto;

import com.ferreteria.catalog_service.model.Region;

import java.time.Instant;

public record RegionResponseDTO(
        Long id,
        String code,
        String name,
        Boolean active,
        Integer displayOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static RegionResponseDTO fromEntity(Region region) {
        return new RegionResponseDTO(
                region.getId(),
                region.getCode(),
                region.getName(),
                region.getIsActive(),
                region.getDisplayOrder(),
                region.getCreatedAt(),
                region.getUpdatedAt()
        );
    }
}
