package com.ferreteria.product_service.repository;

import com.ferreteria.product_service.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, java.util.UUID> {

    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId ORDER BY i.sortOrder ASC, i.createdAt ASC")
    List<ProductImage> findByProductId(java.util.UUID productId);

    Optional<ProductImage> findByProductIdAndPrimaryTrue(java.util.UUID productId);
}