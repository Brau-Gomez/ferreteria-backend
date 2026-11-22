package com.ferreteria.user_service.repository;

import com.ferreteria.user_service.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByRut(String rut);

    boolean existsByEmail(String email);

    boolean existsByRut(String rut);

    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.estado = :estado")
    Page<Usuario> findByRolAndEstado(Usuario.Rol rol, Usuario.EstadoUsuario estado, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE u.estado = :estado")
    Page<Usuario> findByEstado(Usuario.EstadoUsuario estado, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Usuario> findByNombreOrEmailContaining(@Param("nombre") String nombre, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol")
    Page<Usuario> findByRol(Usuario.Rol rol, Pageable pageable);

    long countByRol(Usuario.Rol rol);

    long countByEstado(Usuario.EstadoUsuario estado);
}