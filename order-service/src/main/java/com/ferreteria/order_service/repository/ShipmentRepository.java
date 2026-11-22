package com.ferreteria.order_service.repository;

import com.ferreteria.order_service.model.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    @Query("SELECT s FROM Shipment s WHERE s.order.id = :orderId ORDER BY s.createdAt DESC")
    List<Shipment> findByOrderId(UUID orderId);

    @Query("SELECT s FROM Shipment s WHERE s.status = :status ORDER BY s.createdAt DESC")
    Page<Shipment> findByStatus(Shipment.ShipmentStatus status, Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE s.trackingNumber = :trackingNumber")
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}