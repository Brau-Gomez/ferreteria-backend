package com.ferreteria.product_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final StreamBridge streamBridge;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(ProductEvents.ProductCreatedEvent event) {
        publish("productoCreado-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(ProductEvents.ProductUpdatedEvent event) {
        publish("productoActualizado-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(ProductEvents.ProductStockChangedEvent event) {
        publish("productoStockActualizado-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(ProductEvents.ProductDeletedEvent event) {
        publish("productoEliminado-out-0", event);
    }

    private void publish(String bindingName, Object event) {
        try {
            if (!streamBridge.send(bindingName, event)) {
                log.warn("Evento no enviado binding={} event={}", bindingName, event.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            log.error("Error publicando evento binding={} event={}", bindingName, event.getClass().getSimpleName(), ex);
        }
    }
}
