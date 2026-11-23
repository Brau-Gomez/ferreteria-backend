package com.ferreteria.order_service.controller;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentSimulatorController {

    @PostMapping("/simular")
    public ResponseEntity<Map<String, Object>> simular(@RequestBody PaymentRequest request) {
        String numero = request.getNumeroTarjeta() == null
                ? ""
                : request.getNumeroTarjeta().replaceAll("\\D", "");

        boolean aprobado = numero.length() >= 12 && !"000000000000".equals(numero);
        return ResponseEntity.ok(Map.of(
                "aprobado", aprobado,
                "estado", aprobado ? "APROBADO" : "RECHAZADO",
                "mensaje", aprobado ? "Pago aprobado" : "Pago rechazado",
                "monto", request.getMonto() == null ? BigDecimal.ZERO : request.getMonto(),
                "moneda", request.getMoneda() == null ? "CLP" : request.getMoneda()
        ));
    }

    @Data
    public static class PaymentRequest {
        private String numeroTarjeta;
        private BigDecimal monto;
        private String moneda;
    }
}
