package com.ferreteria.user_service.service;

import com.ferreteria.user_service.dto.DireccionRequestDTO;
import com.ferreteria.user_service.dto.DireccionResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.exception.ResourceNotFoundException;
import com.ferreteria.user_service.model.Direccion;
import com.ferreteria.user_service.model.Usuario;
import com.ferreteria.user_service.repository.DireccionRepository;
import com.ferreteria.user_service.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DireccionResponseDTO crear(UUID usuarioId, DireccionRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // La primera dirección de un usuario pasa a ser la principal automáticamente.
        boolean esPrimera = direccionRepository.countByUsuarioId(usuarioId) == 0;

        Direccion direccion = Direccion.builder()
                .usuario(usuario)
                .alias(request.getAlias())
                .calle(request.getCalle())
                .numero(request.getNumero())
                .departamento(request.getDepartamento())
                .comuna(request.getComuna())
                .region(request.getRegion())
                .codigoPostal(request.getCodigoPostal())
                .referencia(request.getReferencia())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .esPrincipal(esPrimera || Boolean.TRUE.equals(request.getEsPrincipal()))
                .esFacturacion(Boolean.TRUE.equals(request.getEsFacturacion()))
                .esDespacho(Boolean.TRUE.equals(request.getEsDespacho()))
                .build();

        // Solo puede haber una dirección marcada por rol: desmarcamos la anterior antes de guardar.
        if (direccion.getEsPrincipal()) {
            quitarMarca(direccionRepository.findPrincipalByUsuarioId(usuarioId), d -> d.setEsPrincipal(false));
        }
        if (direccion.getEsFacturacion()) {
            quitarMarca(direccionRepository.findFacturacionByUsuarioId(usuarioId), d -> d.setEsFacturacion(false));
        }
        if (direccion.getEsDespacho()) {
            quitarMarca(direccionRepository.findDespachoByUsuarioId(usuarioId), d -> d.setEsDespacho(false));
        }

        Direccion guardada = direccionRepository.save(direccion);
        log.info("Dirección creada id={} usuarioId={}", guardada.getId(), usuarioId);
        return DireccionResponseDTO.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public DireccionResponseDTO obtenerPorId(UUID id) {
        return DireccionResponseDTO.fromEntity(obtenerDireccion(id));
    }

    // La ruta declara el usuario, así que se verifica la pertenencia: sin esto cualquier autenticado
    // leería direcciones ajenas cambiando el id. Se responde 404 para no revelar que existe.
    @Transactional(readOnly = true)
    public DireccionResponseDTO obtenerPorUsuario(UUID usuarioId, UUID id) {
        Direccion direccion = obtenerDireccion(id);
        if (!direccion.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Dirección no encontrada con ID: " + id);
        }
        return DireccionResponseDTO.fromEntity(direccion);
    }

    @Transactional(readOnly = true)
    public List<DireccionResponseDTO> listarPorUsuario(UUID usuarioId) {
        validarUsuario(usuarioId);
        return direccionRepository.findByUsuarioId(usuarioId).stream()
                .map(DireccionResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<DireccionResponseDTO> listarPorUsuarioPaginado(UUID usuarioId, int page, int size) {
        validarUsuario(usuarioId);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("esPrincipal"), Sort.Order.asc("alias")));
        return PageResponseDTO.from(direccionRepository.findByUsuarioId(usuarioId, pageable)
                .map(DireccionResponseDTO::fromEntity));
    }

    public DireccionResponseDTO actualizar(UUID id, DireccionRequestDTO request) {
        Direccion direccion = obtenerDireccion(id);
        UUID usuarioId = direccion.getUsuario().getId();

        direccion.setAlias(request.getAlias());
        direccion.setCalle(request.getCalle());
        direccion.setNumero(request.getNumero());
        direccion.setDepartamento(request.getDepartamento());
        direccion.setComuna(request.getComuna());
        direccion.setRegion(request.getRegion());
        direccion.setCodigoPostal(request.getCodigoPostal());
        direccion.setReferencia(request.getReferencia());
        direccion.setLatitud(request.getLatitud());
        direccion.setLongitud(request.getLongitud());

        // Una marca solo se quita desde los endpoints dedicados: aquí únicamente se activan.
        if (Boolean.TRUE.equals(request.getEsPrincipal()) && !direccion.getEsPrincipal()) {
            quitarMarca(direccionRepository.findPrincipalByUsuarioId(usuarioId), d -> d.setEsPrincipal(false));
            direccion.setEsPrincipal(true);
        }
        if (Boolean.TRUE.equals(request.getEsFacturacion()) && !direccion.getEsFacturacion()) {
            quitarMarca(direccionRepository.findFacturacionByUsuarioId(usuarioId), d -> d.setEsFacturacion(false));
            direccion.setEsFacturacion(true);
        }
        if (Boolean.TRUE.equals(request.getEsDespacho()) && !direccion.getEsDespacho()) {
            quitarMarca(direccionRepository.findDespachoByUsuarioId(usuarioId), d -> d.setEsDespacho(false));
            direccion.setEsDespacho(true);
        }

        Direccion actualizada = direccionRepository.save(direccion);
        log.info("Dirección actualizada id={}", id);
        return DireccionResponseDTO.fromEntity(actualizada);
    }

    public DireccionResponseDTO establecerPrincipal(UUID id) {
        return marcar(id, "esPrincipal");
    }

    public DireccionResponseDTO establecerFacturacion(UUID id) {
        return marcar(id, "esFacturacion");
    }

    public DireccionResponseDTO establecerDespacho(UUID id) {
        return marcar(id, "esDespacho");
    }

    public void eliminar(UUID id) {
        Direccion direccion = obtenerDireccion(id);
        UUID usuarioId = direccion.getUsuario().getId();
        boolean eraPrincipal = direccion.getEsPrincipal();
        boolean eraFacturacion = direccion.getEsFacturacion();
        boolean eraDespacho = direccion.getEsDespacho();

        direccionRepository.delete(direccion);

        // Si se borró la marcada, la siguiente dirección existente hereda la marca para no dejar al usuario sin ella.
        if (eraPrincipal) {
            direccionRepository.findPrincipalByUsuarioId(usuarioId).ifPresent(d -> {
                d.setEsPrincipal(true);
                direccionRepository.save(d);
            });
        }
        if (eraFacturacion) {
            direccionRepository.findFacturacionByUsuarioId(usuarioId).ifPresent(d -> {
                d.setEsFacturacion(true);
                direccionRepository.save(d);
            });
        }
        if (eraDespacho) {
            direccionRepository.findDespachoByUsuarioId(usuarioId).ifPresent(d -> {
                d.setEsDespacho(true);
                direccionRepository.save(d);
            });
        }
        log.info("Dirección eliminada id={}", id);
    }

    @Transactional(readOnly = true)
    public long contarPorUsuario(UUID usuarioId) {
        return direccionRepository.countByUsuarioId(usuarioId);
    }

    private DireccionResponseDTO marcar(UUID id, String marca) {
        Direccion direccion = obtenerDireccion(id);
        UUID usuarioId = direccion.getUsuario().getId();

        switch (marca) {
            case "esPrincipal" -> {
                quitarMarca(direccionRepository.findPrincipalByUsuarioId(usuarioId), d -> d.setEsPrincipal(false));
                direccion.setEsPrincipal(true);
            }
            case "esFacturacion" -> {
                quitarMarca(direccionRepository.findFacturacionByUsuarioId(usuarioId), d -> d.setEsFacturacion(false));
                direccion.setEsFacturacion(true);
            }
            default -> {
                quitarMarca(direccionRepository.findDespachoByUsuarioId(usuarioId), d -> d.setEsDespacho(false));
                direccion.setEsDespacho(true);
            }
        }

        Direccion actualizada = direccionRepository.save(direccion);
        log.info("Dirección id={} marcada como {}", id, marca);
        return DireccionResponseDTO.fromEntity(actualizada);
    }

    // Desmarca la dirección que ocupaba el rol, salvo que sea la misma que estamos marcando.
    private void quitarMarca(java.util.Optional<Direccion> actual, java.util.function.Consumer<Direccion> quitar) {
        actual.ifPresent(d -> {
            quitar.accept(d);
            direccionRepository.save(d);
        });
    }

    private void validarUsuario(UUID usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId);
        }
    }

    private Direccion obtenerDireccion(UUID id) {
        return direccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada con ID: " + id));
    }
}
