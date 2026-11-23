package com.ferreteria.user_service.repository;

import com.ferreteria.user_service.model.PeriodoInteres;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PeriodoInteresRepository extends JpaRepository<PeriodoInteres, UUID> {
    Optional<PeriodoInteres> findByCuentaCorrienteIdAndPeriodo(UUID cuentaCorrienteId, String periodo);
}
