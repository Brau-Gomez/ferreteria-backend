package com.ferreteria.cart_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferreteria.cart_service.model.OutboxEvent;
import com.ferreteria.cart_service.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartEventPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private CartEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new CartEventPublisher(outboxEventRepository, objectMapper);
    }

    @Test
    void debeGuardarEventoItemAgregadoEnOutbox() throws Exception {
        CartEvents.CartItemAddedEvent event = CartEvents.CartItemAddedEvent.of("user-a", "TORN-001");

        publisher.publish(event);

        OutboxEvent guardado = capturarEventoGuardado();
        assertThat(guardado.getBindingName()).isEqualTo("carritoItemAgregado-out-0");
        assertThat(guardado.getEventType()).isEqualTo("carrito.item_agregado");
        assertThat(guardado.getRoutingKey()).isEqualTo("carrito.item_agregado");
        assertThat(guardado.getPayload()).isEqualTo(objectMapper.writeValueAsString(event));
        assertThat(guardado.getAttempts()).isZero();
    }

    @Test
    void debeGuardarEventoItemEliminadoEnOutbox() throws Exception {
        CartEvents.CartItemRemovedEvent event = CartEvents.CartItemRemovedEvent.of("user-a", "TORN-001");

        publisher.publish(event);

        OutboxEvent guardado = capturarEventoGuardado();
        assertThat(guardado.getBindingName()).isEqualTo("carritoItemEliminado-out-0");
        assertThat(guardado.getEventType()).isEqualTo("carrito.item_eliminado");
        assertThat(guardado.getRoutingKey()).isEqualTo("carrito.item_eliminado");
        assertThat(guardado.getPayload()).isEqualTo(objectMapper.writeValueAsString(event));
        assertThat(guardado.getAttempts()).isZero();
    }

    @Test
    void debeGuardarEventoCarritoVaciadoEnOutbox() throws Exception {
        CartEvents.CartClearedEvent event = CartEvents.CartClearedEvent.of("user-a");

        publisher.publish(event);

        OutboxEvent guardado = capturarEventoGuardado();
        assertThat(guardado.getBindingName()).isEqualTo("carritoVaciado-out-0");
        assertThat(guardado.getEventType()).isEqualTo("carrito.vaciado");
        assertThat(guardado.getRoutingKey()).isEqualTo("carrito.vaciado");
        assertThat(guardado.getPayload()).isEqualTo(objectMapper.writeValueAsString(event));
        assertThat(guardado.getAttempts()).isZero();
    }

    private OutboxEvent capturarEventoGuardado() {
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        return captor.getValue();
    }
}
