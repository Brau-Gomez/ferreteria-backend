package com.ferreteria.order_service.exception;

// Excepción para claves de idempotencia duplicadas; el handler responde HTTP 409.
public class IdempotencyException extends RuntimeException {

    public IdempotencyException(String mensaje) {
        super(mensaje);
    }
}