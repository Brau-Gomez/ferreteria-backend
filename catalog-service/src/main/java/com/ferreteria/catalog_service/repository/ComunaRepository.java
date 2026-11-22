package com.ferreteria.catalog_service.repository;

import com.ferreteria.catalog_service.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    Optional<Comuna> findByCode(String code);
    List<Comuna> findByRegionIdAndIsActiveTrueOrderByDisplayOrderAscNameAsc(Long regionId);
    List<Comuna> findByIsActiveTrueOrderByNameAsc();
    boolean existsByCode(String code);
}