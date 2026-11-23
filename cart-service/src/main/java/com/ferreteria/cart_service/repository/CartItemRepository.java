package com.ferreteria.cart_service.repository;

import com.ferreteria.cart_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByUserIdOrderByCreatedAtAsc(String userId);
    Optional<CartItem> findByUserIdAndProductCode(String userId, String productCode);
    long countByUserId(String userId);
    void deleteByUserId(String userId);
}
