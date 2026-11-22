package com.ferreteria.user_service.service;

import com.ferreteria.user_service.dto.MovimientoCuentaCorrienteResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.exception.BadRequestException;
import com.ferreteria.user_service.exception.ResourceNotFoundException;
import com.ferreteria.user_service.model.MovimientoCuentaCorriente;
import com.ferreteria.user_service.repository.MovimientoCuentaCorrienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoCuentaCorrienteService {

    private final MovimientoCuentaCorrienteRepository movimientoRepository;

    public MovimientoCuentaCorrienteResponseDTO obtenerPorId(UUID id) {
        MovimientoCuentaCorriente movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con ID: " + id));
        return MovimientoCuentaCorrienteResponseDTO.fromEntity(movimiento);
    }

    public List<MovimientoCuentaCorrienteResponseDTO> listarPorCuentaCorriente(UUID cuentaCorrienteId) {
        return movimientoRepository.findByCuentaCorrienteId(cuentaCorrienteId).stream()
                .map(MovimientoCuentaCorrienteResponseDTO::fromEntity)
                .toList();
    }

    public PageResponseDTO<MovimientoCuentaCorrienteResponseDTO> listarPorCuentaCorrientePaginado(
            UUID cuentaCorrienteId, int page, int size, String tipo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        var movimientosPage = (tipo == null || tipo.isBlank())
                ? movimientoRepository.findByCuentaCorrienteId(cuentaCorrienteId, pageable)
                : movimientoRepository.findByCuentaCorrienteIdAndTipo(cuentaCorrienteId, parseTipo(tipo), pageable);

        return PageResponseDTO.from(movimientosPage.map(MovimientoCuentaCorrienteResponseDTO::fromEntity));
    }

    public PageResponseDTO<MovimientoCuentaCorrienteResponseDTO> listarPorPeriodo(
            UUID cuentaCorrienteId, Instant inicio, Instant fin, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponseDTO.from(movimientoRepository
                .findByCuentaCorrienteIdAndCreatedAtBetween(cuentaCorrienteId, inicio, fin, pageable)
                .map(MovimientoCuentaCorrienteResponseDTO::fromEntity));
    }

    public List<MovimientoCuentaCorrienteResponseDTO> listarPorReferencia(UUID referenciaId, String referenciaTipo) {
        return movimientoRepository.findByReferencia(referenciaId, referenciaTipo).stream()
                .map(MovimientoCuentaCorrienteResponseDTO::fromEntity)
                .toList();
    }

    public BigDecimal obtenerTotalPorPeriodo(UUID cuentaCorrienteId, Instant inicio, Instant fin) {
        return movimientoRepository.sumMontoByCuentaCorrienteIdAndPeriodo(cuentaCorrienteId, inicio, fin)
                .orElse(BigDecimal.ZERO);
    }

    private MovimientoCuentaCorriente.TipoMovimiento parseTipo(String tipo) {
        try {
            return MovimientoCuentaCorriente.TipoMovimiento.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Tipo de movimiento inválido: " + tipo);
        }
    }
}
