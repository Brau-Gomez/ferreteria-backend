package com.ferreteria.cart_service.repository;

import com.ferreteria.cart_service.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT e FROM OutboxEvent e
            WHERE e.sentAt IS NULL AND e.attempts < 20
            ORDER BY e.createdAt ASC
            """)
    List<OutboxEvent> findPendingForUpdate();

    List<OutboxEvent> findBySentAtIsNullOrderByCreatedAtAsc();
}
