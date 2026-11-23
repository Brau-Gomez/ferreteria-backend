package com.ferreteria.order_service.dto;

import com.ferreteria.order_service.model.Order;
import com.ferreteria.order_service.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


// Boleta simulada: documento que la tienda devuelve al confirmar la compra.
// Los montos salen de la orden persistida (nunca del cliente).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoletaSimuladaDTO {

    private String folio;
    private Instant fecha;
    private String emisor;
    private String rutEmisor;
    private String direccionEmisor;
    private List<OrderItemResponseDTO> items;
    private BigDecimal neto;
    private BigDecimal iva;
    private BigDecimal despacho;
    private BigDecimal total;
    private String direccionEntrega;
    private String tipoEntrega;
    private String medioPago;
    private String transaccionId;
    private String estadoPago;
    private String estadoOrden;

    static BoletaSimuladaDTO fromEntity(Order order) {
        Payment pago = (order.getPayments() == null || order.getPayments().isEmpty())
                ? null
                : order.getPayments().get(order.getPayments().size() - 1);

        return BoletaSimuladaDTO.builder()
                .folio(order.getId() == null ? "BOL-SIMULADA"
                        : "BOL-" + order.getId().toString().substring(0, 8).toUpperCase())
                // createdAt se asigna al hacer flush; la boleta se arma antes.
                .fecha(pago != null && pago.getPaidAt() != null ? pago.getPaidAt() : Instant.now())
                .emisor("Ferretería Los Maestros")
                .rutEmisor("76.543.210-K")
                .direccionEmisor("Av. Francisco de Aguirre 1234, La Serena")
                .items(order.getItems() == null ? List.of()
                        : order.getItems().stream().map(OrderItemResponseDTO::fromEntity).toList())
                .neto(order.getSubtotal())
                .iva(order.getTax())
                .despacho(order.getShippingCost())
                .total(order.getTotal())
                .direccionEntrega(order.getShippingAddress())
                .tipoEntrega(order.getNotes())
                .medioPago(pago != null && pago.getPaymentMethod() != null ? pago.getPaymentMethod().name() : null)
                .transaccionId(pago != null ? pago.getTransactionId() : null)
                .estadoPago(pago != null && pago.getStatus() != null ? pago.getStatus().name() : null)
                .estadoOrden(order.getStatus() != null ? order.getStatus().name() : null)
                .build();
    }
}
