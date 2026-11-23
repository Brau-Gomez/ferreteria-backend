package com.ferreteria.user_service.repository;

import com.ferreteria.user_service.model.MovimientoCuentaCorriente;
import com.ferreteria.user_service.model.CuentaCorriente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovimientoCuentaCorrienteRepository extends JpaRepository<MovimientoCuentaCorriente, UUID> {

    long countByCuentaCorrienteId(UUID cuentaCorrienteId);

    List<MovimientoCuentaCorriente> findByCuentaCorriente(CuentaCorriente cuentaCorriente);

    Page<MovimientoCuentaCorriente> findByCuentaCorriente(CuentaCorriente cuentaCorriente, Pageable pageable);

    List<MovimientoCuentaCorriente> findByCuentaCorrienteId(UUID cuentaCorrienteId);

    Page<MovimientoCuentaCorriente> findByCuentaCorrienteId(UUID cuentaCorrienteId, Pageable pageable);

    @Query("SELECT m FROM MovimientoCuentaCorriente m WHERE m.cuentaCorriente.id = :cuentaCorrienteId AND m.tipo = :tipo")
    Page<MovimientoCuentaCorriente> findByCuentaCorrienteIdAndTipo(
            @Param("cuentaCorrienteId") UUID cuentaCorrienteId,
            @Param("tipo") MovimientoCuentaCorriente.TipoMovimiento tipo,
            Pageable pageable);

    @Query("SELECT m FROM MovimientoCuentaCorriente m WHERE m.cuentaCorriente.id = :cuentaCorrienteId AND m.createdAt BETWEEN :inicio AND :fin")
    List<MovimientoCuentaCorriente> findByCuentaCorrienteIdAndCreatedAtBetween(
            @Param("cuentaCorrienteId") UUID cuentaCorrienteId,
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin);

    Page<MovimientoCuentaCorriente> findByCuentaCorrienteIdAndCreatedAtBetween(
            @Param("cuentaCorrienteId") UUID cuentaCorrienteId,
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin,
            Pageable pageable);

    @Query("SELECT m FROM MovimientoCuentaCorriente m WHERE m.referenciaId = :referenciaId AND m.referenciaTipo = :referenciaTipo")
    List<MovimientoCuentaCorriente> findByReferencia(@Param("referenciaId") UUID referenciaId, @Param("referenciaTipo") String referenciaTipo);

    @Query("SELECT SUM(m.monto) FROM MovimientoCuentaCorriente m WHERE m.cuentaCorriente.id = :cuentaCorrienteId AND m.createdAt BETWEEN :inicio AND :fin")
    Optional<java.math.BigDecimal> sumMontoByCuentaCorrienteIdAndPeriodo(
            @Param("cuentaCorrienteId") UUID cuentaCorrienteId,
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin);
}