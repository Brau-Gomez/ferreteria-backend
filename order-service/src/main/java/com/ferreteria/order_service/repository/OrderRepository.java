package com.ferreteria.order_service.repository;

import com.ferreteria.order_service.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdAndStatus(UUID userId, Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :desde AND o.createdAt <= :hasta ORDER BY o.createdAt DESC")
    Page<Order> findByDateRange(java.time.Instant desde, java.time.Instant hasta, Pageable pageable);

    List<Order> findByStatusIn(List<Order.OrderStatus> statuses);
}