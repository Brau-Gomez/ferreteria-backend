package com.ferreteria.product_service.repository;

import com.ferreteria.product_service.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, java.util.UUID> {

    Optional<Product> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(java.util.UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.code = :code")
    Optional<Product> findByCodeForUpdate(String code);

    boolean existsByCode(String code);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.active = true ORDER BY p.createdAt DESC")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.active = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :filtro, '%')) OR LOWER(p.code) LIKE LOWER(CONCAT('%', :filtro, '%'))) ORDER BY p.createdAt DESC")
    Page<Product> findByFiltro(String filtro, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.active = true AND p.category.id = :categoryId ORDER BY p.createdAt DESC")
    Page<Product> findByCategoryId(java.util.UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.featured = true ORDER BY p.createdAt DESC")
    List<Product> findFeatured();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock <= p.minStock")
    List<Product> findLowStock();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}