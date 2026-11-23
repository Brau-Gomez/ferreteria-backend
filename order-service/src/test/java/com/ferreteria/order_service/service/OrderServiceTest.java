package com.ferreteria.order_service.service;

import com.ferreteria.order_service.client.CatalogClient;
import com.ferreteria.order_service.client.ProductClient;
import com.ferreteria.order_service.dto.OrderRequestDTO;
import com.ferreteria.order_service.dto.OrderResponseDTO;
import com.ferreteria.order_service.event.OrderEvents;
import com.ferreteria.order_service.exception.BadRequestException;
import com.ferreteria.order_service.model.Order;
import com.ferreteria.order_service.model.Payment;
import com.ferreteria.order_service.repository.OrderItemRepository;
import com.ferreteria.order_service.repository.OrderRepository;
import com.ferreteria.order_service.repository.PaymentRepository;
import com.ferreteria.order_service.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ProductClient productClient;
    @Mock private CatalogClient catalogClient;

    @Test
    void creaOrdenConDatosRealesYReservaStock() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);
        UUID userId = UUID.randomUUID();
        String communeId = "1";
        String regionId = "10";
        OrderRequestDTO request = request(communeId, regionId);

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(catalogClient.getComuna(1L)).thenReturn(
                new CatalogClient.ComunaView(1L, "1", "Santiago",
                        new CatalogClient.RegionView(10L, "10", "RM", true), true));
        when(catalogClient.getCoverage(1L)).thenReturn(
                new CatalogClient.CoverageView(1L, true, BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(50000), 2));
        ProductClient.ProductView product = new ProductClient.ProductView(
                UUID.randomUUID(), "TORN-001", "Tornillo real",
                BigDecimal.valueOf(2500), 10, true);
        when(productClient.getByCode("TORN-001")).thenReturn(product);
        when(productClient.reserve("TORN-001", 2)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = service.crear(userId, request, "pedido-1");

        assertThat(response.getTotal()).isEqualByComparingTo("10950.00");
        verify(productClient).reserve("TORN-001", 2);
        verify(eventPublisher).publishEvent(any(OrderEvents.OrderCreatedEvent.class));
    }

    @Test
    void simulaPagoYDevuelveBoletaConLosDatosDeLaOrden() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);
        UUID userId = UUID.randomUUID();
        OrderRequestDTO request = request("1", "10");

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(catalogClient.getComuna(1L)).thenReturn(
                new CatalogClient.ComunaView(1L, "1", "Santiago",
                        new CatalogClient.RegionView(10L, "10", "RM", true), true));
        when(catalogClient.getCoverage(1L)).thenReturn(
                new CatalogClient.CoverageView(1L, true, BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(50000), 2));
        ProductClient.ProductView producto = new ProductClient.ProductView(
                UUID.randomUUID(), "TORN-001", "Tornillo real",
                BigDecimal.valueOf(2500), 10, true);
        when(productClient.getByCode("TORN-001")).thenReturn(producto);
        when(productClient.reserve("TORN-001", 2)).thenReturn(producto);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = service.crear(userId, request, "boleta-1");

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getBoleta()).isNotNull();
        assertThat(response.getBoleta().getTotal()).isEqualByComparingTo("10950.00");
        assertThat(response.getBoleta().getItems()).hasSize(1);
        assertThat(response.getBoleta().getEstadoPago()).isEqualTo("COMPLETED");
        assertThat(response.getBoleta().getTransaccionId()).startsWith("SIM-");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void rechazaStockInsuficienteAntesDeCrear() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);
        String communeId = "1";
        String regionId = "10";
        OrderRequestDTO request = request(communeId, regionId);

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(catalogClient.getComuna(1L)).thenReturn(
                new CatalogClient.ComunaView(1L, "1", "Santiago",
                        new CatalogClient.RegionView(10L, "10", "RM", true), true));
        when(catalogClient.getCoverage(1L)).thenReturn(
                new CatalogClient.CoverageView(1L, true, BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(50000), 2));
        when(productClient.getByCode("TORN-001")).thenReturn(new ProductClient.ProductView(
                UUID.randomUUID(), "TORN-001", "Tornillo real",
                BigDecimal.valueOf(2500), 1, true));

        assertThatThrownBy(() -> service.crear(UUID.randomUUID(), request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(orderRepository, never()).save(any());
        verify(productClient, never()).reserve(anyString(), anyInt());
    }

    @Test
    void rechazaPagoConMontoInvalido() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);

        assertThatThrownBy(() -> service.agregarPago(
                UUID.randomUUID(), Payment.PaymentMethod.CREDIT_CARD, BigDecimal.ZERO, "tx-1"))
                .isInstanceOf(com.ferreteria.order_service.exception.BadRequestException.class)
                .hasMessageContaining("mayor a cero");
        verify(orderRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void rechazaTransaccionDuplicada() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status(Order.OrderStatus.PENDING)
                .total(BigDecimal.valueOf(10000))
                .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByTransactionId("tx-1"))
                .thenReturn(Optional.of(Payment.builder().transactionId("tx-1").build()));

        assertThatThrownBy(() -> service.agregarPago(
                orderId, Payment.PaymentMethod.CREDIT_CARD, BigDecimal.valueOf(1000), "tx-1"))
                .isInstanceOf(com.ferreteria.order_service.exception.IdempotencyException.class);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rechazaTransicionDeEstadoInvalida() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status(Order.OrderStatus.DELIVERED)
                .build();
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.actualizarEstado(orderId, Order.OrderStatus.CANCELLED))
                .isInstanceOf(com.ferreteria.order_service.exception.BadRequestException.class)
                .hasMessageContaining("Transición de estado inválida");
    }

    @Test
    void rechazaCancelacionSegundaVez() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(Order.OrderStatus.PENDING)
                .items(List.of())
                .build();
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        service.cancelar(orderId, "prueba", userId, false);
        assertThatThrownBy(() -> service.cancelar(orderId, "prueba", userId, false))
                .isInstanceOf(com.ferreteria.order_service.exception.BadRequestException.class)
                .hasMessageContaining("CANCELLED");
    }

    @Test
    void rechazaCancelacionPorUsuarioAjeno() {
        OrderService service = new OrderService(orderRepository, orderItemRepository,
                shipmentRepository, paymentRepository, eventPublisher, productClient, catalogClient);
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .userId(UUID.randomUUID())
                .status(Order.OrderStatus.PENDING)
                .items(List.of())
                .build();
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelar(orderId, "prueba", UUID.randomUUID(), false))
                .isInstanceOf(com.ferreteria.order_service.exception.BadRequestException.class)
                .hasMessageContaining("permiso");
    }

    private OrderRequestDTO request(String communeId, String regionId) {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setShippingAddress("Calle ejemplo 123");
        request.setShippingCommuneId(communeId);
        request.setShippingRegionId(regionId);
        request.setNotes("Prueba");
        OrderRequestDTO.OrderItemRequestDTO item = new OrderRequestDTO.OrderItemRequestDTO();
        item.setProductCode("TORN-001");
        item.setQuantity(2);
        request.setItems(List.of(item));
        return request;
    }
}
