package com.ferreteria.catalog_service.exception;

// Representa una búsqueda sin resultados; el handler global la traduce a HTTP 404.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}