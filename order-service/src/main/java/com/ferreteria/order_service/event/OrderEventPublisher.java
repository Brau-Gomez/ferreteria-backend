package com.ferreteria.order_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferreteria.order_service.model.OutboxEvent;
import com.ferreteria.order_service.repository.OutboxEventRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publish(OrderEvents.OrderCreatedEvent event) {
        publish("ordenCreada-out-0", event, event.eventType());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publish(OrderEvents.OrderStatusChangedEvent event) {
        publish("ordenEstadoCambiado-out-0", event, event.eventType());
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
