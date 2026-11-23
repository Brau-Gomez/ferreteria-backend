package com.ferreteria.order_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class ProductClient {

    private final RestClient restClient;
    private final String internalSecret;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${PRODUCT_SERVICE_URL:http://localhost:8082}") String productServiceUrl,
            @Value("${INTERNAL_API_SECRET:}") String internalSecret) {
        this.internalSecret = internalSecret;
        this.restClient = restClientBuilder
                .baseUrl(productServiceUrl)
                .build();
    }

    public ProductView getByCode(String code) {
        try {
            return restClient.get()
                    .uri("/api/products/code/{code}", code)
                    .header("X-Internal-Service", internalSecret)
                    .retrieve()
                    .body(ProductView.class);
        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();
            if (status.is4xxClientError()) {
                throw new ProductUnavailableException("Producto no encontrado: " + code);
            }
            log.error("product-service no disponible al consultar {}", code, ex);
            throw new ProductUnavailableException("Servicio de productos no disponible");
        } catch (RestClientException ex) {
            log.error("product-service no disponible al consultar {}", code, ex);
            throw new ProductUnavailableException("Servicio de productos no disponible");
        }
    }

    public ProductView reserve(String code, int quantity) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products/code/{code}/reserve")
                            .queryParam("cantidad", quantity)
                            .build(code))
                    .header("X-Internal-Service", internalSecret)
                    .retrieve()
                    .body(ProductView.class);
        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();
            if (status.is4xxClientError()) {
                throw new ProductUnavailableException(ex.getResponseBodyAsString());
            }
            log.error("No se pudo reservar stock del producto {}", code, ex);
            throw new ProductUnavailableException("Servicio de productos no disponible");
        } catch (RestClientException ex) {
            log.error("No se pudo reservar stock del producto {}", code, ex);
            throw new ProductUnavailableException("Servicio de productos no disponible");
        }
    }

    public void release(String code, int quantity) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products/code/{code}/release")
                            .queryParam("cantidad", quantity)
                            .build(code))
                    .header("X-Internal-Service", internalSecret)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error("No se pudo liberar stock del producto {}", code, ex);
            throw new ProductUnavailableException("No se pudo liberar el stock reservado");
        } catch (RestClientException ex) {
            log.error("No se pudo liberar stock del producto {}", code, ex);
            throw new ProductUnavailableException("No se pudo liberar el stock reservado");
        }
    }

    public record ProductView(
            UUID id,
            String code,
            String name,
            BigDecimal price,
            int stock,
            boolean active
    ) {}

    public static class ProductUnavailableException extends RuntimeException {
        public ProductUnavailableException(String message) {
            super(message);
        }
    }
}
