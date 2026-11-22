package com.ferreteria.order_service.exception;

// Errores causados por datos incorrectos del cliente; el handler global responde HTTP 400.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String mensaje) {
        super(mensaje);
    }

    public BadRequestException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}