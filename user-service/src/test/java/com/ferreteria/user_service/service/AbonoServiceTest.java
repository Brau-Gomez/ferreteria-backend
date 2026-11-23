package com.ferreteria.user_service.service;

import com.ferreteria.user_service.model.Abono;
import com.ferreteria.user_service.model.CuentaCorriente;
import com.ferreteria.user_service.repository.AbonoRepository;
import com.ferreteria.user_service.repository.CuentaCorrienteRepository;
import com.ferreteria.user_service.repository.MovimientoCuentaCorrienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbonoServiceTest {

    @Mock private AbonoRepository abonoRepository;
    @Mock private CuentaCorrienteRepository cuentaCorrienteRepository;
    @Mock private MovimientoCuentaCorrienteRepository movimientoRepository;
    @Mock private CuentaCorrienteService cuentaCorrienteService;

    @Test
    void aplicarDosVecesNoGeneraSegundoImpacto() {
        AbonoService service = new AbonoService(
                abonoRepository, cuentaCorrienteRepository, movimientoRepository, cuentaCorrienteService);
        UUID abonoId = UUID.randomUUID();
        Abono abono = Abono.builder()
                .id(abonoId)
                .estado(Abono.EstadoAbono.APLICADO)
                .cuentaCorriente(CuentaCorriente.builder().id(UUID.randomUUID()).build())
                .build();
        when(abonoRepository.findByIdForUpdate(abonoId)).thenReturn(Optional.of(abono));

        assertThatThrownBy(() -> service.aplicar(abonoId, "admin"))
                .isInstanceOf(com.ferreteria.user_service.exception.BadRequestException.class)
                .hasMessageContaining("PENDIENTES");
        verify(cuentaCorrienteRepository, never()).findByIdForUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazarAbonoAplicadoNoEstaPermitido() {
        AbonoService service = new AbonoService(
                abonoRepository, cuentaCorrienteRepository, movimientoRepository, cuentaCorrienteService);
        UUID abonoId = UUID.randomUUID();
        Abono abono = Abono.builder()
                .id(abonoId)
                .estado(Abono.EstadoAbono.APLICADO)
                .build();
        when(abonoRepository.findByIdForUpdate(abonoId)).thenReturn(Optional.of(abono));

        assertThatThrownBy(() -> service.rechazar(abonoId, "admin", "motivo"))
                .isInstanceOf(com.ferreteria.user_service.exception.BadRequestException.class)
                .hasMessageContaining("PENDIENTES");
        verify(abonoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
