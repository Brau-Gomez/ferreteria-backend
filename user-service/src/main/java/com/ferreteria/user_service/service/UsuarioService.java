package com.ferreteria.user_service.service;

import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.dto.CrearPerfilUsuarioRequestDTO;
import com.ferreteria.user_service.dto.UsuarioRequestDTO;
import com.ferreteria.user_service.dto.UsuarioResponseDTO;
import com.ferreteria.user_service.exception.BadRequestException;
import com.ferreteria.user_service.exception.ResourceNotFoundException;
import com.ferreteria.user_service.event.UsuarioEvents;
import com.ferreteria.user_service.model.Usuario;
import com.ferreteria.user_service.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }
        if (request.getRut() != null && usuarioRepository.existsByRut(request.getRut())) {
            throw new BadRequestException("El RUT ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .telefono(request.getTelefono())
                .rut(request.getRut())
                .rol(request.getRol())
                .estado(Usuario.EstadoUsuario.ACTIVO)
                .emailVerificado(false)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado id={} email={}", guardado.getId(), guardado.getEmail());
        UsuarioResponseDTO response = UsuarioResponseDTO.fromEntity(guardado);
        eventPublisher.publishEvent(UsuarioEvents.UsuarioCreatedEvent.of(guardado));
        return response;
    }

    public UsuarioResponseDTO crearPerfilDesdeAuth(CrearPerfilUsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }
        if (request.getRut() != null && usuarioRepository.existsByRut(request.getRut())) {
            throw new BadRequestException("El RUT ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .telefono(request.getTelefono())
                .rut(request.getRut())
                .rol(request.getRol() == null ? Usuario.Rol.CLIENTE : request.getRol())
                .estado(Usuario.EstadoUsuario.ACTIVO)
                .emailVerificado(false)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Perfil creado desde auth-service id={} email={}", guardado.getId(), guardado.getEmail());
        UsuarioResponseDTO response = UsuarioResponseDTO.fromEntity(guardado);
        eventPublisher.publishEvent(UsuarioEvents.UsuarioCreatedEvent.of(guardado));
        return response;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(UUID id) {
        return UsuarioResponseDTO.fromEntity(obtenerUsuario(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email " + email));
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<UsuarioResponseDTO> listar(int page, int size, String sortBy, String sortDir, String filtro) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Usuario> usuariosPage;
        if (filtro != null && !filtro.isBlank()) {
            usuariosPage = usuarioRepository.findByNombreOrEmailContaining(filtro.trim(), pageable);
        } else {
            usuariosPage = usuarioRepository.findAll(pageable);
        }

        return PageResponseDTO.from(usuariosPage.map(UsuarioResponseDTO::fromEntity));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<UsuarioResponseDTO> listarPorRol(Usuario.Rol rol, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombreCompleto").ascending());
        return PageResponseDTO.from(usuarioRepository.findByRol(rol, pageable).map(UsuarioResponseDTO::fromEntity));
    }

    public UsuarioResponseDTO actualizar(UUID id, UsuarioRequestDTO request) {
        Usuario usuario = obtenerUsuario(id);

        if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }
        if (request.getRut() != null && !request.getRut().equals(usuario.getRut()) && usuarioRepository.existsByRut(request.getRut())) {
            throw new BadRequestException("El RUT ya está registrado");
        }

        usuario.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setTelefono(request.getTelefono());
        usuario.setRut(request.getRut());
        usuario.setRol(request.getRol());

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado id={}", id);
        UsuarioResponseDTO response = UsuarioResponseDTO.fromEntity(actualizado);
        eventPublisher.publishEvent(UsuarioEvents.UsuarioUpdatedEvent.of(actualizado));
        return response;
    }

    public UsuarioResponseDTO actualizarEstado(UUID id, Usuario.EstadoUsuario estado) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setEstado(estado);
        log.info("Estado de usuario actualizado id={} estado={}", id, estado);
        Usuario actualizado = usuarioRepository.save(usuario);
        eventPublisher.publishEvent(UsuarioEvents.UsuarioUpdatedEvent.of(actualizado));
        return UsuarioResponseDTO.fromEntity(actualizado);
    }

    public UsuarioResponseDTO actualizarRol(UUID id, Usuario.Rol rol) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setRol(rol);
        log.info("Rol de usuario actualizado id={} rol={}", id, rol);
        Usuario actualizado = usuarioRepository.save(usuario);
        eventPublisher.publishEvent(UsuarioEvents.UsuarioUpdatedEvent.of(actualizado));
        return UsuarioResponseDTO.fromEntity(actualizado);
    }

    @Transactional(readOnly = true)
    public boolean existePorId(UUID id) {
        return usuarioRepository.existsById(id);
    }

    public void eliminar(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            log.warn("Intento de eliminar usuario inexistente id={}", id);
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
        log.info("Usuario eliminado id={}", id);
        eventPublisher.publishEvent(UsuarioEvents.UsuarioDeletedEvent.of(id));
    }

    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existePorRut(String rut) {
        return rut != null && usuarioRepository.existsByRut(rut);
    }

    @Transactional(readOnly = true)
    public long contarPorRol(Usuario.Rol rol) {
        return usuarioRepository.countByRol(rol);
    }

    @Transactional(readOnly = true)
    public long contarPorEstado(Usuario.EstadoUsuario estado) {
        return usuarioRepository.countByEstado(estado);
    }

    // Recupera la entidad o lanza 404; centraliza el mensaje para no repetirlo en cada método.
    private Usuario obtenerUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Set<String> allowed = Set.of("nombreCompleto", "email", "rol", "estado", "createdAt");
        String sortField = allowed.contains(sortBy) ? sortBy : "nombreCompleto";
        int boundedSize = Math.min(Math.max(size, 1), 100);
        int boundedPage = Math.max(page, 0);
        return PageRequest.of(boundedPage, boundedSize, Sort.by(direction, sortField));
    }
}
