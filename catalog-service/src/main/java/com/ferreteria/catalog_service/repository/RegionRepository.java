package com.ferreteria.catalog_service.repository;

import com.ferreteria.catalog_service.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByCode(String code);
    List<Region> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
    boolean existsByCode(String code);
}