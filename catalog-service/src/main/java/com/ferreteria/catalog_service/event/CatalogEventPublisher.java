package com.ferreteria.catalog_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventPublisher {

    private final StreamBridge streamBridge;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.RegionCreatedEvent event) {
        publish("regionCreada-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.RegionUpdatedEvent event) {
        publish("regionActualizada-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.RegionDeletedEvent event) {
        publish("regionEliminada-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.ComunaCreatedEvent event) {
        publish("comunaCreada-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.ComunaUpdatedEvent event) {
        publish("comunaActualizada-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.ComunaDeletedEvent event) {
        publish("comunaEliminada-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.CoberturaUpdatedEvent event) {
        publish("coberturaActualizada-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(CatalogEvents.CoberturaDeletedEvent event) {
        publish("coberturaEliminada-out-0", event);
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
