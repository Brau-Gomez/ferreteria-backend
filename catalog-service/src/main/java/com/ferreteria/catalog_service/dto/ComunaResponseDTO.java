package com.ferreteria.catalog_service.dto;

import com.ferreteria.catalog_service.model.Comuna;

import java.time.Instant;

public record ComunaResponseDTO(
        Long id,
        String code,
        String name,
        Long regionId,
        Boolean active,
        Integer displayOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static ComunaResponseDTO fromEntity(Comuna comuna) {
        return new ComunaResponseDTO(
                comuna.getId(),
                comuna.getCode(),
                comuna.getName(),
                comuna.getRegion() != null ? comuna.getRegion().getId() : null,
                comuna.getIsActive(),
                comuna.getDisplayOrder(),
                comuna.getCreatedAt(),
                comuna.getUpdatedAt()
        );
    }
}
