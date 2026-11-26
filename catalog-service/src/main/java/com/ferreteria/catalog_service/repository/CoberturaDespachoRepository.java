package com.ferreteria.catalog_service.repository;

import com.ferreteria.catalog_service.model.CoberturaDespacho;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoberturaDespachoRepository extends JpaRepository<CoberturaDespacho, Long> {
    // La comuna y su región se cargan junto con la cobertura: el controller devuelve la entidad
    // y, sin esto, Jackson intenta leer la relación LAZY fuera de la sesión (LazyInitializationException).
    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    Optional<CoberturaDespacho> findByComunaId(Long comunaId);

    @EntityGraph(attributePaths = {"comuna", "comuna.region"})
    List<CoberturaDespacho> findByIsCoveredTrueAndIsActiveTrue();
    boolean existsByComunaId(Long comunaId);
    void deleteByComunaId(Long comunaId);
}