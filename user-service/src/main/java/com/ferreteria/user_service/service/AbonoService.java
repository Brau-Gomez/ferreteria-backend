package com.ferreteria.user_service.service;

import com.ferreteria.user_service.dto.AbonoRequestDTO;
import com.ferreteria.user_service.dto.AbonoResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.exception.BadRequestException;
import com.ferreteria.user_service.exception.ResourceNotFoundException;
import com.ferreteria.user_service.model.Abono;
import com.ferreteria.user_service.model.CuentaCorriente;
import com.ferreteria.user_service.model.MovimientoCuentaCorriente;
import com.ferreteria.user_service.repository.AbonoRepository;
import com.ferreteria.user_service.repository.CuentaCorrienteRepository;
import com.ferreteria.user_service.repository.MovimientoCuentaCorrienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Transactional
public class AbonoService {

    private final AbonoRepository abonoRepository;
    private final CuentaCorrienteRepository cuentaCorrienteRepository;
    private final MovimientoCuentaCorrienteRepository movimientoRepository;
    private final CuentaCorrienteService cuentaCorrienteService;

    public AbonoResponseDTO crear(UUID cuentaCorrienteId, AbonoRequestDTO request) {
        CuentaCorriente cuenta = obtenerCuenta(cuentaCorrienteId);

        Abono abono = Abono.builder()
                .cuentaCorriente(cuenta)
                .monto(request.getMonto())
                .tipo(request.getTipo())
                .estado(Abono.EstadoAbono.PENDIENTE)
                .referenciaExterna(request.getReferenciaExterna())
                .medioPago(request.getMedioPago())
                .fechaAbono(request.getFechaAbono())
                .observaciones(request.getObservaciones())
                .build();

        Abono guardado = abonoRepository.save(abono);
        log.info("Abono registrado id={} cuenta={} monto={} tipo={}",
                guardado.getId(), cuentaCorrienteId, guardado.getMonto(), guardado.getTipo());
        return AbonoResponseDTO.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public AbonoResponseDTO obtenerPorId(UUID id) {
        return AbonoResponseDTO.fromEntity(obtenerAbono(id));
    }

    @Transactional(readOnly = true)
    public List<AbonoResponseDTO> listarPorCuentaCorriente(UUID cuentaCorrienteId) {
        return abonoRepository.findByCuentaCorrienteId(cuentaCorrienteId).stream()
                .map(AbonoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<AbonoResponseDTO> listarPorCuentaCorrientePaginado(UUID cuentaCorrienteId, int page, int size,
                                                                             String estado) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaAbono").descending());

        var abonosPage = (estado == null || estado.isBlank())
                ? abonoRepository.findByCuentaCorrienteId(cuentaCorrienteId, pageable)
                : abonoRepository.findByCuentaCorrienteIdAndEstado(cuentaCorrienteId, parseEstado(estado), pageable);

        return PageResponseDTO.from(abonosPage.map(AbonoResponseDTO::fromEntity));
    }

    // Antes esta consulta ignoraba la cuenta y devolvía abonos de todas: el endpoint vive bajo
    // /cuentas-corrientes/{id}/abonos, así que el filtro por cuenta es obligatorio.
    @Transactional(readOnly = true)
    public PageResponseDTO<AbonoResponseDTO> listarPorEstado(UUID cuentaCorrienteId, String estado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaAbono").descending());
        return PageResponseDTO.from(abonoRepository
                .findByCuentaCorrienteIdAndEstado(cuentaCorrienteId, parseEstado(estado), pageable)
                .map(AbonoResponseDTO::fromEntity));
    }

    public AbonoResponseDTO aplicar(UUID id, String procesadoPor) {
        Abono abono = obtenerAbono(id);

        if (abono.getEstado() != Abono.EstadoAbono.PENDIENTE) {
            throw new BadRequestException("Solo se pueden aplicar abonos PENDIENTES. Estado actual: " + abono.getEstado());
        }
        // Un reembolso saca dinero de la cuenta: aplicarlo como pago reduciría la deuda por error.
        if (abono.getTipo() == Abono.TipoAbono.REEMBOLSO) {
            throw new BadRequestException("Un reembolso no se aplica como pago de la cuenta corriente");
        }
        if (abono.getMonto() == null || abono.getMonto().signum() <= 0) {
            throw new BadRequestException("El monto del abono debe ser mayor que cero");
        }
        CuentaCorriente cuenta = cuentaCorrienteRepository.findByIdForUpdate(abono.getCuentaCorriente().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta corriente no encontrada"));

        abono.setEstado(Abono.EstadoAbono.APLICADO);
        abono.setFechaAplicacion(Instant.now());
        abono.setProcesadoPor(procesadoPor);
        Abono actualizado = abonoRepository.save(abono);

        // Reutiliza la aritmética de saldos de CuentaCorrienteService y deja registrado el movimiento.
        cuentaCorrienteService.aplicarPago(cuenta, abono.getMonto(),
                MovimientoCuentaCorriente.TipoMovimiento.ABONO_PAGO, describir(abono),
                abono.getId(), "ABONO");

        log.info("Abono aplicado id={} monto={} por={}", id, abono.getMonto(), procesadoPor);
        return AbonoResponseDTO.fromEntity(actualizado);
    }

    public AbonoResponseDTO rechazar(UUID id, String procesadoPor, String motivo) {
        Abono abono = obtenerAbono(id);

        if (abono.getEstado() != Abono.EstadoAbono.PENDIENTE) {
            throw new BadRequestException("Solo se pueden rechazar abonos PENDIENTES. Estado actual: " + abono.getEstado());
        }

        abono.setEstado(Abono.EstadoAbono.RECHAZADO);
        abono.setProcesadoPor(procesadoPor);
        abono.setObservaciones(concatenar(abono.getObservaciones(), "RECHAZADO: " + motivo));

        log.info("Abono rechazado id={} por={} motivo={}", id, procesadoPor, motivo);
        return AbonoResponseDTO.fromEntity(abonoRepository.save(abono));
    }

    public AbonoResponseDTO anular(UUID id, String procesadoPor) {
        Abono abono = obtenerAbono(id);

        if (abono.getEstado() != Abono.EstadoAbono.APLICADO) {
            throw new BadRequestException("Solo se pueden anular abonos APLICADOS. Estado actual: " + abono.getEstado());
        }

        CuentaCorriente cuenta = cuentaCorrienteRepository.findByIdForUpdate(abono.getCuentaCorriente().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta corriente no encontrada"));
        BigDecimal utilizadoAnterior = cuenta.getSaldoUtilizado();
        BigDecimal disponibleAnterior = cuenta.getSaldoDisponible();

        // Revertir: la deuda vuelve a subir. Si excede el límite, el disponible queda negativo y la cuenta va a mora.
        cuenta.setSaldoUtilizado(utilizadoAnterior.add(abono.getMonto()));
        cuenta.setSaldoDisponible(cuenta.getLimiteCredito().subtract(cuenta.getSaldoUtilizado()));
        if (cuenta.getSaldoDisponible().signum() < 0) {
            cuenta.setEstado(CuentaCorriente.EstadoCuenta.EN_MORA);
        }
        cuentaCorrienteRepository.save(cuenta);

        abono.setEstado(Abono.EstadoAbono.ANULADO);
        abono.setProcesadoPor(procesadoPor);
        abono.setObservaciones(concatenar(abono.getObservaciones(), "ANULADO por: " + procesadoPor));
        Abono actualizado = abonoRepository.save(abono);

        movimientoRepository.save(MovimientoCuentaCorriente.builder()
                .cuentaCorriente(cuenta)
                .monto(abono.getMonto()) // positivo: vuelve a ser deuda
                .tipo(MovimientoCuentaCorriente.TipoMovimiento.AJUSTE_MANUAL)
                .descripcion("Anulación de abono " + abono.getId())
                .referenciaId(abono.getId())
                .referenciaTipo("ABONO_ANULADO")
                .saldoAnterior(utilizadoAnterior)
                .saldoNuevo(cuenta.getSaldoUtilizado())
                .saldoDisponibleAnterior(disponibleAnterior)
                .saldoDisponibleNuevo(cuenta.getSaldoDisponible())
                .build());

        log.info("Abono anulado id={} por={}", id, procesadoPor);
        return AbonoResponseDTO.fromEntity(actualizado);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalAplicado(UUID cuentaCorrienteId) {
        return abonoRepository.sumMontoByCuentaCorrienteIdAndEstado(cuentaCorrienteId, Abono.EstadoAbono.APLICADO)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalPendiente(UUID cuentaCorrienteId) {
        return abonoRepository.sumMontoByCuentaCorrienteIdAndEstado(cuentaCorrienteId, Abono.EstadoAbono.PENDIENTE)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<AbonoResponseDTO> obtenerPendientesAntiguos(int diasAntiguedad) {
        Instant fechaLimite = Instant.now().minusSeconds(diasAntiguedad * 24L * 60 * 60);
        return abonoRepository.findPendientesAntiguos(Abono.EstadoAbono.PENDIENTE, fechaLimite).stream()
                .map(AbonoResponseDTO::fromEntity)
                .toList();
    }

    private String describir(Abono abono) {
        return "Abono " + abono.getTipo()
                + (abono.getReferenciaExterna() != null ? " - Ref: " + abono.getReferenciaExterna() : "");
    }

    private String concatenar(String original, String nuevo) {
        return (original != null ? original + " | " : "") + nuevo;
    }

    private Abono obtenerAbono(UUID id) {
        return abonoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abono no encontrado con ID: " + id));
    }

    private CuentaCorriente obtenerCuenta(UUID cuentaCorrienteId) {
        return cuentaCorrienteRepository.findById(cuentaCorrienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta corriente no encontrada con ID: " + cuentaCorrienteId));
    }

    private Abono.EstadoAbono parseEstado(String estado) {
        try {
            return Abono.EstadoAbono.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Estado de abono inválido: " + estado);
        }
    }
}
