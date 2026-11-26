package com.ferreteria.user_service.service;

import com.ferreteria.user_service.dto.CuentaCorrienteRequestDTO;
import com.ferreteria.user_service.dto.CuentaCorrienteResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.exception.BadRequestException;
import com.ferreteria.user_service.exception.ResourceNotFoundException;
import com.ferreteria.user_service.exception.SaldoInsuficienteException;
import com.ferreteria.user_service.model.CuentaCorriente;
import com.ferreteria.user_service.model.MovimientoCuentaCorriente;
import com.ferreteria.user_service.model.Usuario;
import com.ferreteria.user_service.repository.CuentaCorrienteRepository;
import com.ferreteria.user_service.repository.MovimientoCuentaCorrienteRepository;
import com.ferreteria.user_service.repository.UsuarioRepository;
import com.ferreteria.user_service.repository.AbonoRepository;
import com.ferreteria.user_service.repository.PeriodoInteresRepository;
import com.ferreteria.user_service.model.PeriodoInteres;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CuentaCorrienteService {

    private final CuentaCorrienteRepository cuentaCorrienteRepository;
    private final MovimientoCuentaCorrienteRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AbonoRepository abonoRepository;
    private final PeriodoInteresRepository periodoInteresRepository;

    public CuentaCorrienteResponseDTO crear(UUID usuarioId, CuentaCorrienteRequestDTO request) {
        if (cuentaCorrienteRepository.existsByUsuarioId(usuarioId)) {
            throw new BadRequestException("El usuario ya tiene una cuenta corriente");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        if (!Usuario.Rol.CONTRATISTA.equals(usuario.getRol())) {
            throw new BadRequestException("Solo los usuarios con rol CONTRATISTA pueden tener cuenta corriente");
        }

        CuentaCorriente cuenta = CuentaCorriente.builder()
                .usuario(usuario)
                .limiteCredito(request.getLimiteCredito())
                .saldoDisponible(request.getLimiteCredito())
                .saldoUtilizado(BigDecimal.ZERO)
                .diaCorte(request.getDiaCorte())
                .diaVencimiento(request.getDiaVencimiento())
                .estado(CuentaCorriente.EstadoCuenta.ACTIVA)
                .tasaInteresMensual(request.getTasaInteresMensual())
                .requiereGarantia(request.getRequiereGarantia())
                .observaciones(request.getObservaciones())
                .build();

        CuentaCorriente guardada = cuentaCorrienteRepository.save(cuenta);

        // Apertura: el estado previo es inexistente, por eso la deuda anterior vale cero.
        registrarMovimiento(guardada, request.getLimiteCredito(), MovimientoCuentaCorriente.TipoMovimiento.APERTURA_CREDITO,
                "Apertura de cuenta corriente con límite de " + request.getLimiteCredito(),
                null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, request.getLimiteCredito());

        log.info("Cuenta corriente creada id={} usuarioId={} límite={}", guardada.getId(), usuarioId, request.getLimiteCredito());
        return CuentaCorrienteResponseDTO.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public CuentaCorrienteResponseDTO obtenerPorId(UUID id) {
        return CuentaCorrienteResponseDTO.fromEntity(obtenerCuenta(id));
    }

    @Transactional(readOnly = true)
    public CuentaCorrienteResponseDTO obtenerPorId(UUID id, UUID userId, boolean puedeAdministrar) {
        CuentaCorriente cuenta = obtenerCuenta(id);
        if (!puedeAdministrar
                && (cuenta.getUsuario() == null || !cuenta.getUsuario().getId().equals(userId))) {
            throw new BadRequestException("No tienes permiso para consultar esta cuenta corriente");
        }
        return CuentaCorrienteResponseDTO.fromEntity(cuenta);
    }

    @Transactional(readOnly = true)
    public CuentaCorrienteResponseDTO obtenerPorUsuarioId(UUID usuarioId) {
        CuentaCorriente cuenta = cuentaCorrienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta corriente no encontrada para usuario: " + usuarioId));
        return CuentaCorrienteResponseDTO.fromEntity(cuenta);
    }


        /**
     * Cuenta corriente del usuario autenticado, buscada por su correo.
     * auth-service y user-service usan ids distintos; el correo es el dato común.
     */
    @Transactional(readOnly = true)
    public CuentaCorrienteResponseDTO obtenerPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("El token no contiene el correo del usuario");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        CuentaCorriente cuenta = cuentaCorrienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no tiene cuenta corriente"));
        return CuentaCorrienteResponseDTO.fromEntity(cuenta);
    }
    
    @Transactional(readOnly = true)
    public CuentaCorrienteResponseDTO obtenerPorUsuarioId(UUID usuarioId, UUID autenticado,
                                                          boolean puedeAdministrar) {
        if (!puedeAdministrar && !usuarioId.equals(autenticado)) {
            throw new BadRequestException("No tienes permiso para consultar esta cuenta corriente");
        }
        return obtenerPorUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CuentaCorrienteResponseDTO> listar(int page, int size, String estado) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CuentaCorriente> cuentasPage;
        if (estado != null && !estado.isBlank()) {
            cuentasPage = cuentaCorrienteRepository.findByEstado(parseEstado(estado), pageable);
        } else {
            cuentasPage = cuentaCorrienteRepository.findAll(pageable);
        }

        return PageResponseDTO.from(cuentasPage.map(CuentaCorrienteResponseDTO::fromEntity));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CuentaCorrienteResponseDTO> listarPorEstado(String estado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("saldoUtilizado").descending());
        return PageResponseDTO.from(cuentaCorrienteRepository.findByEstado(parseEstado(estado), pageable)
                .map(CuentaCorrienteResponseDTO::fromEntity));
    }

    public CuentaCorrienteResponseDTO actualizar(UUID id, CuentaCorrienteRequestDTO request) {
        CuentaCorriente cuenta = obtenerCuentaParaModificar(id);
        validarCuentaOperable(cuenta);

        BigDecimal limiteAnterior = cuenta.getLimiteCredito();
        BigDecimal disponibleAnterior = cuenta.getSaldoDisponible();
        BigDecimal utilizado = cuenta.getSaldoUtilizado();

        cuenta.setDiaCorte(request.getDiaCorte());
        cuenta.setDiaVencimiento(request.getDiaVencimiento());
        cuenta.setTasaInteresMensual(request.getTasaInteresMensual());
        cuenta.setRequiereGarantia(request.getRequiereGarantia());
        cuenta.setObservaciones(request.getObservaciones());

        if (limiteAnterior.compareTo(request.getLimiteCredito()) != 0) {
            BigDecimal nuevoDisponible = request.getLimiteCredito().subtract(utilizado);
            if (nuevoDisponible.signum() < 0) {
                throw new SaldoInsuficienteException(
                        "El nuevo límite " + request.getLimiteCredito() + " es menor que la deuda actual " + utilizado);
            }
            cuenta.setLimiteCredito(request.getLimiteCredito());
            cuenta.setSaldoDisponible(nuevoDisponible);

            BigDecimal diferencia = request.getLimiteCredito().subtract(limiteAnterior);
            registrarMovimiento(cuenta, diferencia.abs(),
                    diferencia.signum() > 0 ? MovimientoCuentaCorriente.TipoMovimiento.APERTURA_CREDITO
                            : MovimientoCuentaCorriente.TipoMovimiento.REDUCCION_CREDITO,
                    (diferencia.signum() > 0 ? "Aumento" : "Reducción") + " de límite de crédito en " + diferencia.abs(),
                    null, null, utilizado, utilizado, disponibleAnterior, nuevoDisponible);
        }

        CuentaCorriente actualizada = cuentaCorrienteRepository.save(cuenta);
        log.info("Cuenta corriente actualizada id={}", id);
        return CuentaCorrienteResponseDTO.fromEntity(actualizada);
    }

    public CuentaCorrienteResponseDTO actualizarEstado(UUID id, String estado) {
        CuentaCorriente cuenta = obtenerCuentaParaModificar(id);
        cuenta.setEstado(parseEstado(estado));
        log.info("Estado de cuenta corriente id={} -> {}", id, cuenta.getEstado());
        return CuentaCorrienteResponseDTO.fromEntity(cuentaCorrienteRepository.save(cuenta));
    }

    public CuentaCorrienteResponseDTO actualizarLimiteCredito(UUID id, BigDecimal nuevoLimite) {
        validarMontoPositivo(nuevoLimite, "El límite de crédito");
        CuentaCorriente cuenta = obtenerCuentaParaModificar(id);
        validarCuentaOperable(cuenta);

        BigDecimal limiteAnterior = cuenta.getLimiteCredito();
        BigDecimal disponibleAnterior = cuenta.getSaldoDisponible();
        BigDecimal utilizado = cuenta.getSaldoUtilizado();
        BigDecimal nuevoDisponible = nuevoLimite.subtract(utilizado);

        if (nuevoDisponible.signum() < 0) {
            throw new SaldoInsuficienteException("No se puede reducir el límite por debajo del saldo utilizado");
        }

        cuenta.setLimiteCredito(nuevoLimite);
        cuenta.setSaldoDisponible(nuevoDisponible);

        BigDecimal diferencia = nuevoLimite.subtract(limiteAnterior);
        registrarMovimiento(cuenta, diferencia.abs(),
                diferencia.signum() > 0 ? MovimientoCuentaCorriente.TipoMovimiento.APERTURA_CREDITO
                        : MovimientoCuentaCorriente.TipoMovimiento.REDUCCION_CREDITO,
                (diferencia.signum() > 0 ? "Aumento" : "Reducción") + " de límite de crédito en " + diferencia.abs(),
                null, null, utilizado, utilizado, disponibleAnterior, nuevoDisponible);

        CuentaCorriente actualizada = cuentaCorrienteRepository.save(cuenta);
        log.info("Límite de cuenta corriente id={} actualizado a {}", id, nuevoLimite);
        return CuentaCorrienteResponseDTO.fromEntity(actualizada);
    }

    public CuentaCorrienteResponseDTO aplicarCargo(UUID id, BigDecimal monto, String descripcion,
                                                   UUID referenciaId, String referenciaTipo) {
        validarMontoPositivo(monto, "El cargo");
        CuentaCorriente cuenta = obtenerCuentaParaModificar(id);

        if (!cuenta.tieneCupoDisponible(monto)) {
            throw new SaldoInsuficienteException("Saldo disponible insuficiente. Disponible: "
                    + cuenta.getSaldoDisponible() + ", requerido: " + monto);
        }
        if (cuenta.getEstado() != CuentaCorriente.EstadoCuenta.ACTIVA) {
            throw new BadRequestException("La cuenta corriente no está activa: " + cuenta.getEstado());
        }

        BigDecimal utilizadoAnterior = cuenta.getSaldoUtilizado();
        BigDecimal disponibleAnterior = cuenta.getSaldoDisponible();

        cuenta.setSaldoUtilizado(utilizadoAnterior.add(monto));
        cuenta.setSaldoDisponible(disponibleAnterior.subtract(monto));

        CuentaCorriente actualizada = cuentaCorrienteRepository.save(cuenta);

        registrarMovimiento(actualizada, monto, MovimientoCuentaCorriente.TipoMovimiento.CARGO_COMPRA,
                descripcion, referenciaId, referenciaTipo,
                utilizadoAnterior, actualizada.getSaldoUtilizado(),
                disponibleAnterior, actualizada.getSaldoDisponible());

        log.info("Cargo aplicado cuenta={} monto={} referencia={}", id, monto, referenciaId);
        return CuentaCorrienteResponseDTO.fromEntity(actualizada);
    }

    public CuentaCorrienteResponseDTO aplicarAbono(UUID id, BigDecimal monto, String descripcion,
                                                   UUID referenciaId, String referenciaTipo) {
        validarMontoPositivo(monto, "El abono");
        CuentaCorriente cuenta = obtenerCuentaParaModificar(id);
        aplicarPago(cuenta, monto, MovimientoCuentaCorriente.TipoMovimiento.ABONO_PAGO,
                descripcion, referenciaId, referenciaTipo);
        log.info("Abono aplicado cuenta={} monto={} referencia={}", id, monto, referenciaId);
        return CuentaCorrienteResponseDTO.fromEntity(cuenta);
    }

    public CuentaCorrienteResponseDTO procesarInteresMensual(UUID id) {
        CuentaCorriente cuenta = obtenerCuentaParaModificar(id);
        validarCuentaOperable(cuenta);
        String periodo = java.time.YearMonth.now().toString();
        if (periodoInteresRepository.findByCuentaCorrienteIdAndPeriodo(id, periodo).isPresent()) {
            throw new BadRequestException("El interés mensual ya fue procesado para el período " + periodo);
        }

        if (cuenta.getSaldoUtilizado().signum() == 0) {
            return CuentaCorrienteResponseDTO.fromEntity(cuenta); // sin deuda no hay interés
        }

        BigDecimal interes = cuenta.getSaldoUtilizado()
                .multiply(cuenta.getTasaInteresMensual())
                .setScale(2, RoundingMode.HALF_UP);

        if (interes.signum() <= 0) {
            return CuentaCorrienteResponseDTO.fromEntity(cuenta);
        }

        BigDecimal utilizadoAnterior = cuenta.getSaldoUtilizado();
        BigDecimal disponibleAnterior = cuenta.getSaldoDisponible();

        cuenta.setSaldoUtilizado(utilizadoAnterior.add(interes));
        cuenta.setSaldoDisponible(cuenta.getLimiteCredito().subtract(cuenta.getSaldoUtilizado()));

        // Si el interés empuja la deuda sobre el límite, la cuenta pasa a mora.
        if (cuenta.getSaldoUtilizado().compareTo(cuenta.getLimiteCredito()) > 0) {
            cuenta.setEstado(CuentaCorriente.EstadoCuenta.EN_MORA);
        }

        CuentaCorriente actualizada = cuentaCorrienteRepository.save(cuenta);

        registrarMovimiento(actualizada, interes, MovimientoCuentaCorriente.TipoMovimiento.CARGO_INTERES,
                "Interés mensual (" + cuenta.getTasaInteresMensual().multiply(BigDecimal.valueOf(100)) + "%)",
                null, null,
                utilizadoAnterior, actualizada.getSaldoUtilizado(),
                disponibleAnterior, actualizada.getSaldoDisponible());
        periodoInteresRepository.save(PeriodoInteres.builder()
                .cuentaCorriente(actualizada)
                .periodo(periodo)
                .monto(interes)
                .build());

        log.info("Interés procesado cuenta={} monto={}", id, interes);
        return CuentaCorrienteResponseDTO.fromEntity(actualizada);
    }

    public void eliminar(UUID id) {
        CuentaCorriente cuenta = obtenerCuentaParaModificar(id);
        if (cuenta.getSaldoUtilizado().signum() != 0) {
            throw new com.ferreteria.user_service.exception.ConflictException(
                    "No se puede eliminar una cuenta con saldo utilizado");
        }
        if (movimientoRepository.countByCuentaCorrienteId(id) > 0
                || abonoRepository.countByCuentaCorrienteId(id) > 0) {
            throw new com.ferreteria.user_service.exception.ConflictException(
                    "No se puede eliminar una cuenta con historial financiero");
        }
        cuenta.setEstado(CuentaCorriente.EstadoCuenta.CERRADA);
        cuentaCorrienteRepository.save(cuenta);
        log.info("Cuenta corriente cerrada id={}", id);
    }

    @Transactional(readOnly = true)
    public long contarPorEstado(String estado) {
        return cuentaCorrienteRepository.countByEstado(parseEstado(estado));
    }

    @Transactional(readOnly = true)
    public List<CuentaCorrienteResponseDTO> obtenerVencidasParaCorte(Integer diaCorte) {
        return cuentaCorrienteRepository.findByEstadoAndDiaCorte(CuentaCorriente.EstadoCuenta.ACTIVA, diaCorte)
                .stream()
                .map(CuentaCorrienteResponseDTO::fromEntity)
                .toList();
    }

    // Aplica un pago y registra su movimiento. Es package-private para que AbonoService lo reutilice
    // validando un abono, en vez de duplicar la aritmética de saldos.
    void aplicarPago(CuentaCorriente cuenta, BigDecimal monto, MovimientoCuentaCorriente.TipoMovimiento tipo,
                             String descripcion, UUID referenciaId, String referenciaTipo) {
        BigDecimal utilizadoAnterior = cuenta.getSaldoUtilizado();
        BigDecimal disponibleAnterior = cuenta.getSaldoDisponible();

        BigDecimal nuevoUtilizado = utilizadoAnterior.subtract(monto);
        if (nuevoUtilizado.signum() < 0) {
            nuevoUtilizado = BigDecimal.ZERO; // el pago no genera saldo a favor
        }
        cuenta.setSaldoUtilizado(nuevoUtilizado);
        cuenta.setSaldoDisponible(cuenta.getLimiteCredito().subtract(nuevoUtilizado));

        // Moroso que salda la deuda vuelve a quedar activo.
        if (cuenta.getEstado() == CuentaCorriente.EstadoCuenta.EN_MORA && nuevoUtilizado.signum() == 0) {
            cuenta.setEstado(CuentaCorriente.EstadoCuenta.ACTIVA);
        }

        CuentaCorriente actualizada = cuentaCorrienteRepository.save(cuenta);

        registrarMovimiento(actualizada, monto.negate(), tipo, descripcion, referenciaId, referenciaTipo,
                utilizadoAnterior, actualizada.getSaldoUtilizado(),
                disponibleAnterior, actualizada.getSaldoDisponible());
    }

    private void registrarMovimiento(CuentaCorriente cuenta, BigDecimal monto, MovimientoCuentaCorriente.TipoMovimiento tipo,
                                     String descripcion, UUID referenciaId, String referenciaTipo,
                                     BigDecimal saldoAnterior, BigDecimal saldoNuevo,
                                     BigDecimal disponibleAnterior, BigDecimal disponibleNuevo) {
        movimientoRepository.save(MovimientoCuentaCorriente.builder()
                .cuentaCorriente(cuenta)
                .monto(monto)
                .tipo(tipo)
                .descripcion(descripcion)
                .referenciaId(referenciaId)
                .referenciaTipo(referenciaTipo)
                .saldoAnterior(saldoAnterior)
                .saldoNuevo(saldoNuevo)
                .saldoDisponibleAnterior(disponibleAnterior)
                .saldoDisponibleNuevo(disponibleNuevo)
                .build());
    }

    private CuentaCorriente obtenerCuenta(UUID id) {
        return cuentaCorrienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta corriente no encontrada con ID: " + id));
    }

    private CuentaCorriente obtenerCuentaParaModificar(UUID id) {
        return cuentaCorrienteRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta corriente no encontrada con ID: " + id));
    }

    private void validarCuentaOperable(CuentaCorriente cuenta) {
        if (cuenta.getEstado() == CuentaCorriente.EstadoCuenta.CERRADA
                || cuenta.getEstado() == CuentaCorriente.EstadoCuenta.BLOQUEADA) {
            throw new BadRequestException("La cuenta corriente no admite operaciones: " + cuenta.getEstado());
        }
    }

    private void validarMontoPositivo(BigDecimal monto, String etiqueta) {
        if (monto == null || monto.signum() <= 0) {
            throw new BadRequestException(etiqueta + " debe ser mayor que cero");
        }
    }

    private CuentaCorriente.EstadoCuenta parseEstado(String estado) {
        try {
            return CuentaCorriente.EstadoCuenta.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Estado de cuenta inválido: " + estado);
        }
    }
}
