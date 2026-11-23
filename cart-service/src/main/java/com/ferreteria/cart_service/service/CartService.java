package com.ferreteria.cart_service.service;

import com.ferreteria.cart_service.client.ProductClient;
import com.ferreteria.cart_service.client.ProductDTO;
import com.ferreteria.cart_service.dto.*;
import com.ferreteria.cart_service.exception.BadRequestException;
import com.ferreteria.cart_service.exception.ResourceNotFoundException;
import com.ferreteria.cart_service.model.CartItem;
import com.ferreteria.cart_service.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository repository;
    private final ProductClient productClient;

    @Value("${cart.max-items:100}")
    private int maxItems = 100;

    @Transactional(readOnly = true)
    public CartResponseDTO getCart(String userId) {
        List<CartItemResponseDTO> items = repository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(item -> {
                    ProductDTO product = productClient.getByCode(item.getProductCode());
                    return toResponse(item, product);
                })
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponseDTO.builder().items(items).total(total).build();
    }

    @Transactional
    public CartItemResponseDTO addItem(String userId, CartItemRequestDTO request) {
        ProductDTO product = productClient.getByCode(request.getCode());
        validateProduct(product, request.getQuantity());

        return repository.findByUserIdAndProductCode(userId, request.getCode())
                .map(existing -> {
                    int newQuantity = existing.getQuantity() + request.getQuantity();
                    validateProduct(product, newQuantity);
                    existing.setQuantity(newQuantity);
                    existing.setUnitPrice(product.getPrice());
                    return toResponse(repository.save(existing), product);
                })
                .orElseGet(() -> {
                    if (repository.countByUserId(userId) >= maxItems) {
                        throw new BadRequestException("El carrito alcanzó el máximo de productos");
                    }
                    CartItem item = CartItem.builder()
                            .userId(userId)
                            .productCode(request.getCode())
                            .quantity(request.getQuantity())
                            .unitPrice(product.getPrice())
                            .build();
                    return toResponse(repository.save(item), product);
                });
    }

    @Transactional
    public CartItemResponseDTO updateItem(String userId, String code, UpdateCartItemRequestDTO request) {
        CartItem item = repository.findByUserIdAndProductCode(userId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no existe en el carrito"));
        ProductDTO product = productClient.getByCode(code);
        validateProduct(product, request.getQuantity());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());
        return toResponse(repository.save(item), product);
    }

    @Transactional
    public void removeItem(String userId, String code) {
        CartItem item = repository.findByUserIdAndProductCode(userId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no existe en el carrito"));
        repository.delete(item);
    }

    @Transactional
    public void clearCart(String userId) {
        repository.deleteByUserId(userId);
    }

    private void validateProduct(ProductDTO product, int quantity) {
        if (product == null || !product.isActive()) {
            throw new BadRequestException("Producto no disponible");
        }
        if (quantity > product.getStock()) {
            throw new BadRequestException("Stock insuficiente");
        }
    }

    private String extractPrimaryImageUrl(ProductDTO product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        // Prefer primary image, fallback to first
        return product.getImages().stream()
                .filter(img -> img.isPrimary())
                .map(ProductDTO.ProductImageDTO::getUrl)
                .findFirst()
                .orElse(product.getImages().get(0).getUrl());
    }

    private CartItemResponseDTO toResponse(CartItem item, ProductDTO product) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        String imageUrl = extractPrimaryImageUrl(product);
        return CartItemResponseDTO.builder()
                .code(item.getProductCode())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(imageUrl)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .stock(product.getStock())
                .minStock(product.getMinStock())
                .featured(product.isFeatured())
                .build();
    }
}
