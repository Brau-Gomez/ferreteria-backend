package com.ferreteria.user_service.repository;

import com.ferreteria.user_service.model.Direccion;
import com.ferreteria.user_service.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, UUID> {

    List<Direccion> findByUsuario(Usuario usuario);

    List<Direccion> findByUsuarioId(UUID usuarioId);

    @Query("SELECT d FROM Direccion d WHERE d.usuario.id = :usuarioId AND d.esPrincipal = true")
    Optional<Direccion> findPrincipalByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT d FROM Direccion d WHERE d.usuario.id = :usuarioId AND d.esFacturacion = true")
    Optional<Direccion> findFacturacionByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT d FROM Direccion d WHERE d.usuario.id = :usuarioId AND d.esDespacho = true")
    Optional<Direccion> findDespachoByUsuarioId(@Param("usuarioId") UUID usuarioId);

    Page<Direccion> findByUsuarioId(UUID usuarioId, Pageable pageable);

    long countByUsuarioId(UUID usuarioId);

    @Query("SELECT d FROM Direccion d WHERE d.comuna = :comuna AND d.region = :region")
    List<Direccion> findByComunaAndRegion(@Param("comuna") String comuna, @Param("region") String region);
}