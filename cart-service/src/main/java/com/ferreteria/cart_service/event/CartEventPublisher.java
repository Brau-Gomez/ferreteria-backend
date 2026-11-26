package com.ferreteria.cart_service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferreteria.cart_service.model.OutboxEvent;
import com.ferreteria.cart_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publish(CartEvents.CartItemAddedEvent event) {
        publish("carritoItemAgregado-out-0", event, event.eventType());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publish(CartEvents.CartItemRemovedEvent event) {
        publish("carritoItemEliminado-out-0", event, event.eventType());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publish(CartEvents.CartClearedEvent event) {
        publish("carritoVaciado-out-0", event, event.eventType());
    }

    private void publish(String bindingName, Object event, String eventType) {
        try {
            outboxEventRepository.save(OutboxEvent.builder()
                    .eventType(eventType)
                    .bindingName(bindingName)
                    .routingKey(eventType)
                    .payload(objectMapper.writeValueAsString(event))
                    .attempts(0)
                    .build());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar evento " + eventType, ex);
        }
    }
}
