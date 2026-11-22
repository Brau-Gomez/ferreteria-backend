package com.ferreteria.order_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final StreamBridge streamBridge;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderEvents.OrderCreatedEvent event) {
        publish("ordenCreada-out-0", event, event.eventType());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderEvents.OrderStatusChangedEvent event) {
        publish("ordenEstadoCambiado-out-0", event, event.eventType());
    }

    private void publish(String bindingName, Object event, String eventType) {
        try {
            if (!streamBridge.send(bindingName, event)) {
                log.warn("Evento no enviado binding={} eventType={}", bindingName, eventType);
            }
        } catch (Exception ex) {
            log.error("Error publicando evento binding={} eventType={}", bindingName, eventType, ex);
        }
    }
}
