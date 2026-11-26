package com.ferreteria.order_service.assemblers;

import com.ferreteria.order_service.dto.OrderResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderModelAssemblerTest {

    @BeforeEach
    void configurarRequestParaLinks() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiarRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModelGeneraEnlaceSelfYOthers() {
        UUID id = UUID.randomUUID();
        OrderResponseDTO orden = OrderResponseDTO.builder()
                .id(id)
                .status("PENDIENTE")
                .build();

        EntityModel<OrderResponseDTO> modelo = new OrderModelAssembler().toModel(orden);

        assertThat(modelo.getContent()).isSameAs(orden);
        assertThat(modelo.getRequiredLink("self").getHref())
                .endsWith("/orders/v2/" + id);
        // La colección lleva query params (page, size, ...), por eso no se usa endsWith.
        assertThat(modelo.getRequiredLink("orders").getHref())
                .contains("/orders/v2");
    }
}
