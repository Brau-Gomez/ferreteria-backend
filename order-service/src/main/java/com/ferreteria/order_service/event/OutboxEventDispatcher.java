package com.ferreteria.order_service.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferreteria.order_service.model.OutboxEvent;
import com.ferreteria.order_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventDispatcher {

    private final OutboxEventRepository outboxEventRepository;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${OUTBOX_DELAY_MS:5000}")
    @Transactional
    public void dispatchPending() {
        List<OutboxEvent> pending = outboxEventRepository.findPendingForUpdate();
        for (OutboxEvent event : pending) {
            try {
                JsonNode payload = objectMapper.readTree(event.getPayload());
                boolean sent = streamBridge.send(event.getBindingName(), payload);
                if (!sent) {
                    registerFailure(event, "RabbitMQ rechazó el envío");
                    continue;
                }
                event.setSentAt(Instant.now());
                event.setLastError(null);
            } catch (Exception ex) {
                registerFailure(event, ex.getMessage());
                log.warn("Reintento pendiente eventId={} intentos={} error={}",
                        event.getId(), event.getAttempts(), ex.getMessage());
            }
            outboxEventRepository.save(event);
        }
    }

    private void registerFailure(OutboxEvent event, String error) {
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(error);
        if (event.getAttempts() >= 20) {
            log.error("Outbox agotó reintentos eventId={} eventType={} error={}",
                    event.getId(), event.getEventType(), error);
        }
    }
}
