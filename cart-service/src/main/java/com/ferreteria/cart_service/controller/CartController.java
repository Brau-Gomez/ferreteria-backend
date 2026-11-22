package com.ferreteria.cart_service.controller;

import com.ferreteria.cart_service.dto.*;
import com.ferreteria.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cartService.getCart(userId(jwt)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDTO> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId(jwt), request));
    }

    @PatchMapping("/items/{code}")
    public ResponseEntity<CartItemResponseDTO> updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String code,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateItem(userId(jwt), code, request));
    }

    @DeleteMapping("/items/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@AuthenticationPrincipal Jwt jwt, @PathVariable String code) {
        cartService.removeItem(userId(jwt), code);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal Jwt jwt) {
        cartService.clearCart(userId(jwt));
    }

    private String userId(Jwt jwt) {
        Object userId = jwt.getClaim("sub");
        if (userId == null) {
            throw new IllegalStateException("El token no contiene subject");
        }
        return userId.toString();
    }
}
