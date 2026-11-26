package com.ferreteria.cart_service.event;

import java.time.Instant;

public final class CartEvents {

    private CartEvents() {
    }

    public record CartItemAddedEvent(
            String eventType,
            String version,
            String userId,
            String productCode,
            Instant occurredAt
    ) {
        public static CartItemAddedEvent of(String userId, String productCode) {
            return new CartItemAddedEvent(
                    "carrito.item_agregado",
                    "1",
                    userId,
                    productCode,
                    Instant.now()
            );
        }
    }

    public record CartItemRemovedEvent(
            String eventType,
            String version,
            String userId,
            String productCode,
            Instant occurredAt
    ) {
        public static CartItemRemovedEvent of(String userId, String productCode) {
            return new CartItemRemovedEvent(
                    "carrito.item_eliminado",
                    "1",
                    userId,
                    productCode,
                    Instant.now()
            );
        }
    }

    public record CartClearedEvent(
            String eventType,
            String version,
            String userId,
            Instant occurredAt
    ) {
        public static CartClearedEvent of(String userId) {
            return new CartClearedEvent(
                    "carrito.vaciado",
                    "1",
                    userId,
                    Instant.now()
            );
        }
    }
}
