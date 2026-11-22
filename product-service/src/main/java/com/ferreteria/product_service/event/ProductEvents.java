package com.ferreteria.product_service.event;

import com.ferreteria.product_service.model.Product;

import java.time.Instant;
import java.util.UUID;

public final class ProductEvents {

    private ProductEvents() {
    }

    public record ProductCreatedEvent(
            String eventType,
            String version,
            UUID productId,
            String code,
            Instant occurredAt
    ) {
        public static ProductCreatedEvent of(Product product) {
            return new ProductCreatedEvent("producto.creado", "1", product.getId(), product.getCode(), Instant.now());
        }
    }

    public record ProductUpdatedEvent(
            String eventType,
            String version,
            UUID productId,
            String code,
            boolean active,
            Instant occurredAt
    ) {
        public static ProductUpdatedEvent of(Product product) {
            return new ProductUpdatedEvent("producto.actualizado", "1", product.getId(), product.getCode(), product.isActive(), Instant.now());
        }
    }

    public record ProductStockChangedEvent(
            String eventType,
            String version,
            UUID productId,
            String code,
            int previousStock,
            int currentStock,
            Instant occurredAt
    ) {
        public static ProductStockChangedEvent of(Product product, int previousStock) {
            return new ProductStockChangedEvent(
                    "producto.stock_actualizado",
                    "1",
                    product.getId(),
                    product.getCode(),
                    previousStock,
                    product.getStock(),
                    Instant.now()
            );
        }
    }

    public record ProductDeletedEvent(
            String eventType,
            String version,
            UUID productId,
            Instant occurredAt
    ) {
        public static ProductDeletedEvent of(UUID productId) {
            return new ProductDeletedEvent("producto.eliminado", "1", productId, Instant.now());
        }
    }
}
