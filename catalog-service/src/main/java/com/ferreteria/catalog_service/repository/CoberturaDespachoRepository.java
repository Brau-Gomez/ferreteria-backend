package com.ferreteria.catalog_service.repository;

import com.ferreteria.catalog_service.model.CoberturaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoberturaDespachoRepository extends JpaRepository<CoberturaDespacho, Long> {
    Optional<CoberturaDespacho> findByComunaId(Long comunaId);
    List<CoberturaDespacho> findByIsCoveredTrueAndIsActiveTrue();
    boolean existsByComunaId(Long comunaId);
    void deleteByComunaId(Long comunaId);
}