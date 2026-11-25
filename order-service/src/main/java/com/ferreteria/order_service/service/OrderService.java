package com.ferreteria.order_service.service;

import com.ferreteria.order_service.client.CatalogClient;
import com.ferreteria.order_service.client.ProductClient;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ProductClient productClient;
    private final CatalogClient catalogClient;

    public OrderResponseDTO crear(UUID userId, OrderRequestDTO request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (orderRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
                return orderRepository.findByIdempotencyKeyAndUserId(idempotencyKey, userId)
                        .map(OrderResponseDTO::fromEntity)
                        .orElseThrow(() -> new IdempotencyException("La clave de idempotencia pertenece a otra cuenta"));
            }
        }

        validarEnvio(request);

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            ProductClient.ProductView product = productClient.getByCode(itemReq.getProductCode());
            if (!product.active()) {
                throw new BadRequestException("El producto está inactivo: " + itemReq.getProductCode());
            }
            if (product.stock() < itemReq.getQuantity()) {
                throw new BadRequestException("Stock insuficiente para " + itemReq.getProductCode());
            }
            BigDecimal unitPrice = product.price();
            return OrderItem.builder()
                    .productCode(itemReq.getProductCode())
                    .productName(product.name())
                    .unitPrice(unitPrice)
                    .quantity(itemReq.getQuantity())
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();
        }).collect(Collectors.toCollection(ArrayList::new)); // Hibernate necesita una colección mutable

        // Calcular totales: los precios de los productos ya incluyen IVA (19%)
        BigDecimal bruto = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = bruto.divide(BigDecimal.valueOf(1.19), 0, RoundingMode.HALF_UP); // neto
        BigDecimal tax = bruto.subtract(subtotal); // IVA incluido en el precio
        BigDecimal shippingCost = calculateShippingCost(bruto, request.getShippingCommuneId());
        BigDecimal total = bruto.add(shippingCost);

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

        List<OrderItem> reservedItems = new java.util.ArrayList<>();
        try {
            for (OrderItem item : items) {
                productClient.reserve(item.getProductCode(), item.getQuantity());
                reservedItems.add(item);
            }
            Order guardada = orderRepository.save(order);
            registrarPagoSimulado(guardada);
            log.info("Orden creada id={} userId={} total={}", guardada.getId(), userId, total);
            publishOrderCreatedEvent(guardada);
            return OrderResponseDTO.fromEntity(guardada);
        } catch (RuntimeException ex) {
            for (OrderItem item : reservedItems) {
                try {
                    productClient.release(item.getProductCode(), item.getQuantity());
                } catch (RuntimeException releaseEx) {
                    log.error("No se pudo compensar stock de {}", item.getProductCode(), releaseEx);
                }
            }
            throw ex;
        }
    }

    /**
     * Pago simulado: no hay pasarela real, así que el cobro se aprueba en el servidor
     * y la orden queda CONFIRMED. La boleta se arma con los datos ya persistidos.
     */
    private void registrarPagoSimulado(Order order) {
        Payment pago = Payment.builder()
                .order(order)
                .status(Payment.PaymentStatus.COMPLETED)
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .amount(order.getTotal())
                .transactionId("SIM-" + UUID.randomUUID())
                .gatewayResponse("Pago simulado aprobado")
                .paidAt(Instant.now())
                .build();
        paymentRepository.save(pago);
        order.getPayments().add(pago);

        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setConfirmedAt(Instant.now());
        orderRepository.save(order);
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
        Order order = obtenerOrdenParaActualizar(id);
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
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago debe ser mayor a cero");
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new BadRequestException("La transacción de pago es obligatoria");
        }

        Order order = obtenerOrdenParaActualizar(orderId);
        if (order.getStatus() == Order.OrderStatus.CANCELLED
                || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("No se pueden registrar pagos para una orden " + order.getStatus());
        }

        if (paymentRepository.findByTransactionId(transactionId).isPresent()) {
            throw new IdempotencyException("Ya existe un pago con ese transactionId");
        }

        BigDecimal totalPagado = order.getPayments().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoPendiente = order.getTotal().subtract(totalPagado);
        if (monto.compareTo(saldoPendiente) > 0) {
            throw new BadRequestException("El monto excede el saldo pendiente de la orden");
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(metodo)
                .amount(monto)
                .transactionId(transactionId)
                .status(Payment.PaymentStatus.PENDING)
                .build();

        order.getPayments().add(payment);
        paymentRepository.save(payment);

        orderRepository.save(order);
        log.info("Pago agregado ordenId={} monto={} metodo={}", orderId, monto, metodo);

        return OrderResponseDTO.fromEntity(order);
    }

    public void cancelar(UUID id, String motivo) {
        Order order = obtenerOrden(id);
        cancelarOrden(order, motivo);
    }

    public void cancelar(UUID id, String motivo, UUID userId, boolean isAdmin) {
        Order order = obtenerOrdenParaActualizar(id);
        verificarPropietarioOAdmin(order, userId, isAdmin);
        cancelarOrden(order, motivo);
    }

    private void cancelarOrden(Order order, String motivo) {
        UUID id = order.getId();
        Order.OrderStatus estadoAnterior = order.getStatus();

        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("No se puede cancelar una orden en estado " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            productClient.release(item.getProductCode(), item.getQuantity());
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Cancelada: " + motivo);

        orderRepository.save(order);
        log.info("Orden cancelada id={} motivo={}", id, motivo);

        publishOrderStatusChangedEvent(order, estadoAnterior);
    }

    private void verificarPropietarioOAdmin(Order order, UUID userId, boolean isAdmin) {
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para acceder a esta orden");
        }
    }

    private void validarEnvio(OrderRequestDTO request) {
        long communeId;
        long regionId;
        try {
            communeId = Long.parseLong(request.getShippingCommuneId());
            regionId = Long.parseLong(request.getShippingRegionId());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Los IDs de región y comuna deben corresponder al catálogo");
        }

        CatalogClient.ComunaView commune = catalogClient.getComuna(communeId);
        if (commune == null || !Boolean.TRUE.equals(commune.active())) {
            throw new BadRequestException("La comuna no existe o está inactiva");
        }
        if (commune.region() == null
                || commune.region().id() == null
                || commune.region().id() != regionId
                || !Boolean.TRUE.equals(commune.region().active())) {
            throw new BadRequestException("La comuna no pertenece a la región indicada");
        }
        CatalogClient.CoverageView coverage = catalogClient.getCoverage(communeId);
        if (coverage == null || !coverage.covered()) {
            throw new BadRequestException("La comuna no tiene cobertura de despacho");
        }
    }

    private Order obtenerOrden(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
    }

    private Order obtenerOrdenParaActualizar(UUID id) {
        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
    }

    private BigDecimal calculateShippingCost(BigDecimal subtotal, String communeId) {
        long commune;
        try {
            commune = Long.parseLong(communeId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("El ID de comuna no corresponde al catálogo actual");
        }
        CatalogClient.CoverageView coverage = catalogClient.getCoverage(commune);
        if (coverage == null || !coverage.covered()) {
            throw new BadRequestException("La comuna no tiene cobertura de despacho");
        }
        if (coverage.deliveryCost() == null) {
            throw new BadRequestException("La cobertura no tiene costo de despacho configurado");
        }
        if (coverage.minOrderAmount() != null
                && subtotal.compareTo(coverage.minOrderAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        return coverage.deliveryCost();
    }

    private void validarTransicion(Order.OrderStatus desde, Order.OrderStatus hacia) {
        boolean valida = switch (desde) {
            case PENDING -> hacia == Order.OrderStatus.CONFIRMED || hacia == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> hacia == Order.OrderStatus.SHIPPED || hacia == Order.OrderStatus.CANCELLED;
            case SHIPPED -> hacia == Order.OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
            default -> false;
        };
        if (!valida) {
            throw new BadRequestException("Transición de estado inválida: " + desde + " -> " + hacia);
        }
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