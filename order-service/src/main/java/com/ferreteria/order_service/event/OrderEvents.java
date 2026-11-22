package com.ferreteria.order_service.event;

import com.ferreteria.order_service.model.Order;

import java.time.Instant;
import java.util.UUID;

public final class OrderEvents {

    private OrderEvents() {
    }

    public record OrderCreatedEvent(
            String eventType,
            String version,
            UUID orderId,
            UUID userId,
            Instant occurredAt
    ) {
        public static OrderCreatedEvent of(Order order) {
            return new OrderCreatedEvent(
                    "orden.creada",
                    "1",
                    order.getId(),
                    order.getUserId(),
                    Instant.now()
            );
        }
    }

    public record OrderStatusChangedEvent(
            String eventType,
            String version,
            UUID orderId,
            Order.OrderStatus previousStatus,
            Order.OrderStatus currentStatus,
            Instant occurredAt
    ) {
        public static OrderStatusChangedEvent of(Order order, Order.OrderStatus previousStatus) {
            return new OrderStatusChangedEvent(
                    "orden.estado_cambiado",
                    "1",
                    order.getId(),
                    previousStatus,
                    order.getStatus(),
                    Instant.now()
            );
        }
    }
}
