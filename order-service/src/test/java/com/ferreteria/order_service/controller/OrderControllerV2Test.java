package com.ferreteria.order_service.controller;

import com.ferreteria.order_service.assemblers.OrderModelAssembler;
import com.ferreteria.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderControllerV2Test {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderModelAssembler assembler;

    @InjectMocks
    private OrderControllerV2 controller;

    private Jwt jwtConRol(String rol) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "12345678-5")
                .claim("roles", List.of(rol))
                .build();
    }

    @Test
    void elVendedorPuedeVerElDetalleDeCualquierOrdenEnV2() {
        UUID id = UUID.randomUUID();

        controller.buscarPorId(id, jwtConRol("VENDEDOR"));

        verify(orderService).obtenerPorId(eq(id), any(), eq(true));
    }
}
