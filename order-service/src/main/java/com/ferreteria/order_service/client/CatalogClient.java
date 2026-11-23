package com.ferreteria.order_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
@Slf4j
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder restClientBuilder,
            @Value("${CATALOG_SERVICE_URL:http://localhost:8085}") String catalogServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(catalogServiceUrl)
                .build();
    }

    public CoverageView getCoverage(long communeId) {
        try {
            ApiResponse<CoverageView> response = restClient.get()
                    .uri("/api/coverage/check/{comunaId}", communeId)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<ApiResponse<CoverageView>>() {});
            return response == null ? null : response.data();
        } catch (RestClientResponseException ex) {
            log.error("catalog-service no disponible al consultar comuna {}", communeId, ex);
            throw new CatalogUnavailableException("Servicio de catálogo no disponible");
        } catch (RestClientException ex) {
            log.error("catalog-service no disponible al consultar comuna {}", communeId, ex);
            throw new CatalogUnavailableException("Servicio de catálogo no disponible");
        }
    }

    public ComunaView getComuna(long communeId) {
        try {
            ApiResponse<ComunaView> response = restClient.get()
                    .uri("/api/communes/{id}", communeId)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<ApiResponse<ComunaView>>() {});
            return response == null ? null : response.data();
        } catch (RestClientResponseException ex) {
            log.error("catalog-service no disponible al consultar comuna {}", communeId, ex);
            throw new CatalogUnavailableException("Comuna no encontrada o catálogo no disponible");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResponse<T>(boolean success, T data, String message, String errorCode) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoverageView(
            long comunaId,
            @JsonAlias({"covered", "isCovered"}) boolean covered,
            @JsonAlias({"deliveryCost", "delivery_cost"}) BigDecimal deliveryCost,
            @JsonAlias({"minOrderAmount", "min_order_amount"}) BigDecimal minOrderAmount,
            Integer estimatedDays) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ComunaView(
            Long id,
            String code,
            String name,
            RegionView region,
            @JsonAlias({"active", "isActive"}) Boolean active) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RegionView(
            Long id,
            String code,
            String name,
            @JsonAlias({"active", "isActive"}) Boolean active) {}

    public static class CatalogUnavailableException extends RuntimeException {
        public CatalogUnavailableException(String message) {
            super(message);
        }
    }
}
