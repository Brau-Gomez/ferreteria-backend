package com.ferreteria.catalog_service.event;

import com.ferreteria.catalog_service.model.Comuna;
import com.ferreteria.catalog_service.model.CoberturaDespacho;
import com.ferreteria.catalog_service.model.Region;

import java.time.Instant;

public final class CatalogEvents {

    private CatalogEvents() {
    }

    public record RegionCreatedEvent(String eventType, String version, Long regionId, String code, Instant occurredAt) {
        public static RegionCreatedEvent of(Region region) {
            return new RegionCreatedEvent("region.creada", "1", region.getId(), region.getCode(), Instant.now());
        }
    }

    public record RegionUpdatedEvent(String eventType, String version, Long regionId, String code, Boolean active, Instant occurredAt) {
        public static RegionUpdatedEvent of(Region region) {
            return new RegionUpdatedEvent("region.actualizada", "1", region.getId(), region.getCode(), region.getIsActive(), Instant.now());
        }
    }

    public record RegionDeletedEvent(String eventType, String version, Long regionId, Instant occurredAt) {
        public static RegionDeletedEvent of(Long regionId) {
            return new RegionDeletedEvent("region.eliminada", "1", regionId, Instant.now());
        }
    }

    public record ComunaCreatedEvent(String eventType, String version, Long comunaId, Long regionId, String code, Instant occurredAt) {
        public static ComunaCreatedEvent of(Comuna comuna) {
            return new ComunaCreatedEvent("comuna.creada", "1", comuna.getId(), comuna.getRegion().getId(), comuna.getCode(), Instant.now());
        }
    }

    public record ComunaUpdatedEvent(String eventType, String version, Long comunaId, Long regionId, String code, Boolean active, Instant occurredAt) {
        public static ComunaUpdatedEvent of(Comuna comuna) {
            return new ComunaUpdatedEvent("comuna.actualizada", "1", comuna.getId(), comuna.getRegion().getId(), comuna.getCode(), comuna.getIsActive(), Instant.now());
        }
    }

    public record ComunaDeletedEvent(String eventType, String version, Long comunaId, Instant occurredAt) {
        public static ComunaDeletedEvent of(Long comunaId) {
            return new ComunaDeletedEvent("comuna.eliminada", "1", comunaId, Instant.now());
        }
    }

    public record CoberturaUpdatedEvent(String eventType, String version, Long comunaId, Boolean covered, java.math.BigDecimal deliveryCost, Integer estimatedDays, Instant occurredAt) {
        public static CoberturaUpdatedEvent of(CoberturaDespacho coverage) {
            return new CoberturaUpdatedEvent(
                    "cobertura.actualizada",
                    "1",
                    coverage.getComuna().getId(),
                    coverage.getIsCovered(),
                    coverage.getDeliveryCost(),
                    coverage.getEstimatedDays(),
                    Instant.now()
            );
        }
    }

    public record CoberturaDeletedEvent(String eventType, String version, Long comunaId, Instant occurredAt) {
        public static CoberturaDeletedEvent of(Long comunaId) {
            return new CoberturaDeletedEvent("cobertura.eliminada", "1", comunaId, Instant.now());
        }
    }
}
