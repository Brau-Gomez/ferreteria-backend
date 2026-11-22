package com.ferreteria.user_service.repository;

import com.ferreteria.user_service.model.Abono;
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
public interface AbonoRepository extends JpaRepository<Abono, UUID> {

    List<Abono> findByCuentaCorriente(CuentaCorriente cuentaCorriente);

    Page<Abono> findByCuentaCorriente(CuentaCorriente cuentaCorriente, Pageable pageable);

    List<Abono> findByCuentaCorrienteId(UUID cuentaCorrienteId);

    Page<Abono> findByCuentaCorrienteId(UUID cuentaCorrienteId, Pageable pageable);

    @Query("SELECT a FROM Abono a WHERE a.cuentaCorriente.id = :cuentaCorrienteId AND a.estado = :estado")
    Page<Abono> findByCuentaCorrienteIdAndEstado(
            @Param("cuentaCorrienteId") UUID cuentaCorrienteId,
            @Param("estado") Abono.EstadoAbono estado,
            Pageable pageable);

    @Query("SELECT a FROM Abono a WHERE a.cuentaCorriente.id = :cuentaCorrienteId AND a.fechaAbono BETWEEN :inicio AND :fin")
    List<Abono> findByCuentaCorrienteIdAndFechaAbonoBetween(
            @Param("cuentaCorrienteId") UUID cuentaCorrienteId,
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin);

    @Query("SELECT a FROM Abono a WHERE a.estado = :estado")
    Page<Abono> findByEstado(Abono.EstadoAbono estado, Pageable pageable);

    @Query("SELECT a FROM Abono a WHERE a.estado = :estado AND a.fechaAbono < :fecha")
    List<Abono> findPendientesAntiguos(@Param("estado") Abono.EstadoAbono estado, @Param("fecha") Instant fecha);

    long countByCuentaCorrienteIdAndEstado(UUID cuentaCorrienteId, Abono.EstadoAbono estado);

    @Query("SELECT SUM(a.monto) FROM Abono a WHERE a.cuentaCorriente.id = :cuentaCorrienteId AND a.estado = :estado")
    Optional<java.math.BigDecimal> sumMontoByCuentaCorrienteIdAndEstado(
            @Param("cuentaCorrienteId") UUID cuentaCorrienteId,
            @Param("estado") Abono.EstadoAbono estado);

    Optional<Abono> findByReferenciaExterna(String referenciaExterna);
}