package com.ferreteria.product_service.repository;

import com.ferreteria.product_service.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, java.util.UUID> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.sortOrder ASC, c.name ASC")
    Page<Category> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findAllActive();

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}