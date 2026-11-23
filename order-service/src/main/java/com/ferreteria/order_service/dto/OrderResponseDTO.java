package com.ferreteria.order_service.dto;

import com.ferreteria.order_service.model.Order;
import com.ferreteria.order_service.model.OrderItem;
import com.ferreteria.order_service.model.Payment;
import com.ferreteria.order_service.model.Shipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private UUID id;
    private UUID userId;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String shippingAddress;
    private String shippingCommuneId;
    private String shippingRegionId;
    private String trackingNumber;
    private String carrier;
    private String notes;
    private String idempotencyKey;
    private List<OrderItemResponseDTO> items;
    private List<ShipmentResponseDTO> shipments;
    private List<PaymentResponseDTO> payments;
    private BoletaSimuladaDTO boleta;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant confirmedAt;
    private Instant shippedAt;
    private Instant deliveredAt;
    private Instant cancelledAt;

    public static OrderResponseDTO fromEntity(Order order) {
        OrderResponseDTO dto = OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .shippingAddress(order.getShippingAddress())
                .shippingCommuneId(order.getShippingCommuneId())
                .shippingRegionId(order.getShippingRegionId())
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .notes(order.getNotes())
                .idempotencyKey(order.getIdempotencyKey())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .build();

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(OrderItemResponseDTO::fromEntity)
                    .toList());
        }
        if (order.getShipments() != null) {
            dto.setShipments(order.getShipments().stream()
                    .map(ShipmentResponseDTO::fromEntity)
                    .toList());
        }
        if (order.getPayments() != null) {
            dto.setPayments(order.getPayments().stream()
                    .map(PaymentResponseDTO::fromEntity)
                    .toList());
        }
        dto.setBoleta(BoletaSimuladaDTO.fromEntity(order));
        return dto;
    }

    // Versión ligera para listados
    public static OrderResponseDTO fromEntityLite(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .total(order.getTotal())
                .shippingAddress(order.getShippingAddress())
                .trackingNumber(order.getTrackingNumber())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OrderItemResponseDTO {
    private UUID id;
    private String productCode;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;
    private Instant createdAt;

    public static OrderItemResponseDTO fromEntity(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productCode(item.getProductCode())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .createdAt(item.getCreatedAt())
                .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ShipmentResponseDTO {
    private UUID id;
    private String status;
    private String trackingNumber;
    private String carrier;
    private String shippingAddress;
    private Instant estimatedDelivery;
    private Instant actualDelivery;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static ShipmentResponseDTO fromEntity(Shipment shipment) {
        return ShipmentResponseDTO.builder()
                .id(shipment.getId())
                .status(shipment.getStatus() != null ? shipment.getStatus().name() : null)
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .shippingAddress(shipment.getShippingAddress())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .actualDelivery(shipment.getActualDelivery())
                .notes(shipment.getNotes())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PaymentResponseDTO {
    private UUID id;
    private String status;
    private String paymentMethod;
    private BigDecimal amount;
    private String transactionId;
    private String gatewayResponse;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;

    public static PaymentResponseDTO fromEntity(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .gatewayResponse(payment.getGatewayResponse())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
