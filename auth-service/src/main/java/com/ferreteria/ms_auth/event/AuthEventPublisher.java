package com.ferreteria.ms_auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private final StreamBridge streamBridge;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(AuthEvents.AuthUserLoginEvent event) {
        publish("authUserLogin-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(AuthEvents.AuthUserRegisteredEvent event) {
        publish("authUserRegistered-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(AuthEvents.AuthTokenRefreshedEvent event) {
        publish("authTokenRefreshed-out-0", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(AuthEvents.AuthUserLogoutEvent event) {
        publish("authUserLogout-out-0", event);
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
