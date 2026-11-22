package com.ferreteria.order_service.service;

import com.ferreteria.order_service.dto.OrderRequestDTO;
import com.ferreteria.order_service.dto.OrderResponseDTO;
import com.ferreteria.order_service.dto.PageResponseDTO;
import com.ferreteria.order_service.exception.BadRequestException;
import com.ferreteria.order_service.exception.IdempotencyException;
import com.ferreteria.order_service.exception.ResourceNotFoundException;
import com.ferreteria.order_service.event.OrderEvents;
import com.ferreteria.order_service.model.Order;
import com.ferreteria.order_service.model.OrderItem;
import com.ferreteria.order_service.model.Payment;
import com.ferreteria.order_service.model.Shipment;
import com.ferreteria.order_service.repository.OrderRepository;
import com.ferreteria.order_service.repository.OrderItemRepository;
import com.ferreteria.order_service.repository.PaymentRepository;
import com.ferreteria.order_service.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Feign client to product-service would be injected here
    // private final ProductClient productClient;

    public OrderResponseDTO crear(UUID userId, OrderRequestDTO request, String idempotencyKey) {
        // Verificar idempotencia
        if (idempotencyKey != null && orderRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new IdempotencyException("Ya existe una orden con esa clave de idempotencia");
        }

        // Validar y crear items (aquí se llamaría a product-service para validar stock y precio)
        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            // TODO: Llamar a product-service via Feign para validar existencia, stock y obtener precio
            // ProductDTO product = productClient.getProduct(itemReq.getProductCode());
            // if (product.getStock() < itemReq.getQuantity()) throw new BadRequestException("Stock insuficiente");
            //
            // Por ahora usamos datos mock:
            BigDecimal unitPrice = BigDecimal.valueOf(10000); // Precio mock
            return OrderItem.builder()
                    .productCode(itemReq.getProductCode())
                    .productName("Producto " + itemReq.getProductCode()) // Nombre mock
                    .unitPrice(unitPrice)
                    .quantity(itemReq.getQuantity())
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();
        }).toList();

        // Calcular totales
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.19)); // IVA 19%
        BigDecimal shippingCost = calculateShippingCost(subtotal, request.getShippingCommuneId());
        BigDecimal total = subtotal.add(tax).add(shippingCost);

        Order order = Order.builder()
                .userId(userId)
                .status(Order.OrderStatus.PENDING)
                .subtotal(subtotal)
                .tax(tax)
                .shippingCost(shippingCost)
                .total(total)
                .shippingAddress(request.getShippingAddress())
                .shippingCommuneId(request.getShippingCommuneId())
                .shippingRegionId(request.getShippingRegionId())
                .notes(request.getNotes())
                .idempotencyKey(idempotencyKey)
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));

        Order guardada = orderRepository.save(order);
        log.info("Orden creada id={} userId={} total={}", guardada.getId(), userId, total);

        // Publicar evento orden.creada
        publishOrderCreatedEvent(guardada);

        return OrderResponseDTO.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO obtenerPorId(UUID id) {
        return OrderResponseDTO.fromEntity(obtenerOrden(id));
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO obtenerPorId(UUID id, UUID userId, boolean isAdmin) {
        Order order = obtenerOrden(id);
        verificarPropietarioOAdmin(order, userId, isAdmin);
        return OrderResponseDTO.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponseDTO> listarPorUsuario(UUID userId, int page, int size, String sortBy, String sortDir, String status) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Order> ordenesPage;
        if (status != null && !status.isBlank()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                ordenesPage = orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                ordenesPage = orderRepository.findByUserId(userId, pageable);
            }
        } else {
            ordenesPage = orderRepository.findByUserId(userId, pageable);
        }

        return PageResponseDTO.from(ordenesPage.map(OrderResponseDTO::fromEntityLite));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponseDTO> listarTodas(int page, int size, String sortBy, String sortDir, String status) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Order> ordenesPage;
        if (status != null && !status.isBlank()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                ordenesPage = orderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                ordenesPage = orderRepository.findAll(pageable);
            }
        } else {
            ordenesPage = orderRepository.findAll(pageable);
        }

        return PageResponseDTO.from(ordenesPage.map(OrderResponseDTO::fromEntityLite));
    }

    public OrderResponseDTO actualizarEstado(UUID id, Order.OrderStatus nuevoEstado) {
        Order order = obtenerOrden(id);
        Order.OrderStatus estadoAnterior = order.getStatus();

        // Validar transición de estado
        validarTransicion(estadoAnterior, nuevoEstado);

        order.setStatus(nuevoEstado);
        actualizarTimestampSegunEstado(order, nuevoEstado);

        Order actualizada = orderRepository.save(order);
        log.info("Estado de orden actualizado id={} de {} a {}", id, estadoAnterior, nuevoEstado);

        // Publicar evento orden.estado_cambiado
        publishOrderStatusChangedEvent(actualizada, estadoAnterior);

        return OrderResponseDTO.fromEntity(actualizada);
    }

    public OrderResponseDTO agregarPago(UUID orderId, Payment.PaymentMethod metodo, BigDecimal monto, String transactionId) {
        Order order = obtenerOrden(orderId);

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(metodo)
                .amount(monto)
                .transactionId(transactionId)
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        order.getPayments().add(payment);
        paymentRepository.save(payment);

        // Si el pago cubre el total, marcar como completado
        BigDecimal totalPagado = order.getPayments().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPagado.compareTo(order.getTotal()) >= 0) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaidAt(Instant.now());
            if (order.getStatus() == Order.OrderStatus.PENDING) {
                order.setStatus(Order.OrderStatus.CONFIRMED);
                order.setConfirmedAt(Instant.now());
            }
        }

        orderRepository.save(order);
        log.info("Pago agregado ordenId={} monto={} metodo={}", orderId, monto, metodo);

        return OrderResponseDTO.fromEntity(order);
    }

    public void cancelar(UUID id, String motivo) {
        Order order = obtenerOrden(id);
        cancelarOrden(order, motivo);
    }

    public void cancelar(UUID id, String motivo, UUID userId, boolean isAdmin) {
        Order order = obtenerOrden(id);
        verificarPropietarioOAdmin(order, userId, isAdmin);
        cancelarOrden(order, motivo);
    }

    private void cancelarOrden(Order order, String motivo) {
        UUID id = order.getId();

        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("No se puede cancelar una orden en estado " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Cancelada: " + motivo);

        orderRepository.save(order);
        log.info("Orden cancelada id={} motivo={}", id, motivo);

        publishOrderStatusChangedEvent(order, Order.OrderStatus.CANCELLED);
    }

    private void verificarPropietarioOAdmin(Order order, UUID userId, boolean isAdmin) {
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para acceder a esta orden");
        }
    }

    private Order obtenerOrden(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
    }

    private BigDecimal calculateShippingCost(BigDecimal subtotal, UUID communeId) {
        // Envío gratis sobre $50.000
        if (subtotal.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            return BigDecimal.ZERO;
        }
        // Costo base por comuna (mock)
        return BigDecimal.valueOf(5000);
    }

    private void validarTransicion(Order.OrderStatus desde, Order.OrderStatus hacia) {
        // Definir transiciones válidas
        return; // TODO: Implementar validación de transiciones
    }

    private void actualizarTimestampSegunEstado(Order order, Order.OrderStatus estado) {
        switch (estado) {
            case CONFIRMED -> order.setConfirmedAt(Instant.now());
            case SHIPPED -> order.setShippedAt(Instant.now());
            case DELIVERED -> order.setDeliveredAt(Instant.now());
            case CANCELLED -> order.setCancelledAt(Instant.now());
            default -> {}
        }
    }

    private void publishOrderCreatedEvent(Order order) {
        eventPublisher.publishEvent(OrderEvents.OrderCreatedEvent.of(order));
    }

    private void publishOrderStatusChangedEvent(Order order, Order.OrderStatus anterior) {
        eventPublisher.publishEvent(OrderEvents.OrderStatusChangedEvent.of(order, anterior));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        int boundedSize = Math.min(Math.max(size, 1), 100);
        int boundedPage = Math.max(page, 0);
        return PageRequest.of(boundedPage, boundedSize, Sort.by(direction, sortField));
    }
}