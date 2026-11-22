package com.ferreteria.catalog_service.dto;

import com.ferreteria.catalog_service.model.CoberturaDespacho;

import java.math.BigDecimal;

public record CoberturaDespachoResponseDTO(
        Long id,
        Long comunaId,
        Boolean covered,
        BigDecimal deliveryCost,
        BigDecimal minOrderAmount,
        Integer estimatedDays,
        Boolean active
) {
    public static CoberturaDespachoResponseDTO fromEntity(CoberturaDespacho coverage) {
        return new CoberturaDespachoResponseDTO(
                coverage.getId(),
                coverage.getComuna() != null ? coverage.getComuna().getId() : null,
                coverage.getIsCovered(),
                coverage.getDeliveryCost(),
                coverage.getMinOrderAmount(),
                coverage.getEstimatedDays(),
                coverage.getIsActive()
        );
    }
}
