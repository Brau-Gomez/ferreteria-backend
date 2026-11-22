package com.ferreteria.user_service.exception;

// Caso de negocio: el monto supera el cupo de la cuenta corriente. Es un 400, no un 500.
public class SaldoInsuficienteException extends BadRequestException {

    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public SaldoInsuficienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
