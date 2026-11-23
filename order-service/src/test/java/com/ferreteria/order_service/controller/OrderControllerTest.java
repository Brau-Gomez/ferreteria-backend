package com.ferreteria.order_service.controller;

import com.ferreteria.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController controller;

    private Jwt jwtConRol(String rol) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "12345678-5")
                .claim("roles", List.of(rol))
                .build();
    }

    @Test
    void elVendedorVeTodasLasOrdenes() {
        controller.listarMisOrdenes(0, 20, "createdAt", "desc", null, jwtConRol("VENDEDOR"));

        verify(orderService).listarTodas(0, 20, "createdAt", "desc", null);
        verify(orderService, never()).listarPorUsuario(any(), anyInt(), anyInt(), anyString(), anyString(), any());
    }

    @Test
    void elClienteVeSoloSusPropiasOrdenes() {
        controller.listarMisOrdenes(0, 20, "createdAt", "desc", null, jwtConRol("CLIENTE"));

        verify(orderService).listarPorUsuario(any(), eq(0), eq(20), eq("createdAt"), eq("desc"), isNull());
        verify(orderService, never()).listarTodas(anyInt(), anyInt(), anyString(), anyString(), any());
    }
}
