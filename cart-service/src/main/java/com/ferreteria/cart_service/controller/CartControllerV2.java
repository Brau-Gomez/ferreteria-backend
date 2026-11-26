package com.ferreteria.cart_service.controller;

import com.ferreteria.cart_service.assemblers.CartModelAssembler;
import com.ferreteria.cart_service.dto.CartResponseDTO;
import com.ferreteria.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart/v2")
@RequiredArgsConstructor
public class CartControllerV2 {

    private final CartService cartService;
    private final CartModelAssembler assembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<CartResponseDTO> getCart(@AuthenticationPrincipal Jwt jwt) {
        return assembler.toModel(cartService.getCart(userId(jwt)));
    }

    private String userId(Jwt jwt) {
        Object userId = jwt.getClaim("sub");
        if (userId == null) {
            throw new IllegalStateException("El token no contiene subject");
        }
        return userId.toString();
    }
}
