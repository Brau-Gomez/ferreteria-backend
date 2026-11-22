package com.ferreteria.user_service.repository;

import com.ferreteria.user_service.model.CuentaCorriente;
import com.ferreteria.user_service.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CuentaCorrienteRepository extends JpaRepository<CuentaCorriente, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CuentaCorriente c WHERE c.id = :id")
    Optional<CuentaCorriente> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CuentaCorriente c WHERE c.usuario.id = :usuarioId")
    Optional<CuentaCorriente> findByUsuarioIdForUpdate(@Param("usuarioId") UUID usuarioId);

    Optional<CuentaCorriente> findByUsuario(Usuario usuario);

    Optional<CuentaCorriente> findByUsuarioId(UUID usuarioId);

    boolean existsByUsuarioId(UUID usuarioId);

    @Query("SELECT c FROM CuentaCorriente c WHERE c.estado = :estado")
    Page<CuentaCorriente> findByEstado(CuentaCorriente.EstadoCuenta estado, Pageable pageable);

    @Query("SELECT c FROM CuentaCorriente c WHERE c.estado = :estado AND c.saldoDisponible < :montoMinimo")
    Page<CuentaCorriente> findByEstadoAndSaldoDisponibleLessThan(
            @Param("estado") CuentaCorriente.EstadoCuenta estado,
            @Param("montoMinimo") java.math.BigDecimal montoMinimo,
            Pageable pageable);

    @Query("SELECT c FROM CuentaCorriente c WHERE c.estado = :estado AND c.saldoUtilizado > :montoMinimo")
    Page<CuentaCorriente> findByEstadoAndSaldoUtilizadoGreaterThan(
            @Param("estado") CuentaCorriente.EstadoCuenta estado,
            @Param("montoMinimo") java.math.BigDecimal montoMinimo,
            Pageable pageable);

    long countByEstado(CuentaCorriente.EstadoCuenta estado);

    @Query("SELECT c FROM CuentaCorriente c WHERE c.estado = :estado AND c.diaCorte = :diaCorte")
    List<CuentaCorriente> findByEstadoAndDiaCorte(
            @Param("estado") CuentaCorriente.EstadoCuenta estado,
            @Param("diaCorte") Integer diaCorte);
}