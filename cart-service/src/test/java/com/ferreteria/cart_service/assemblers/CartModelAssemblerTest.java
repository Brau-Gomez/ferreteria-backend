package com.ferreteria.cart_service.assemblers;

import com.ferreteria.cart_service.dto.CartResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartModelAssemblerTest {

    @BeforeEach
    void configurarRequestParaLinks() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiarRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModelGeneraEnlaceSelf() {
        CartResponseDTO carrito = CartResponseDTO.builder()
                .total(new BigDecimal("1000.00"))
                .build();

        EntityModel<CartResponseDTO> modelo = new CartModelAssembler().toModel(carrito);

        assertThat(modelo.getContent()).isSameAs(carrito);
        assertThat(modelo.getRequiredLink("self").getHref())
                .endsWith("/cart/v2");
    }
}
