package com.ferreteria.user_service.service;

import com.ferreteria.user_service.exception.SaldoInsuficienteException;
import com.ferreteria.user_service.model.CuentaCorriente;
import com.ferreteria.user_service.model.MovimientoCuentaCorriente;
import com.ferreteria.user_service.repository.CuentaCorrienteRepository;
import com.ferreteria.user_service.repository.MovimientoCuentaCorrienteRepository;
import com.ferreteria.user_service.repository.UsuarioRepository;
import com.ferreteria.user_service.repository.AbonoRepository;
import com.ferreteria.user_service.repository.PeriodoInteresRepository;
import com.ferreteria.user_service.model.PeriodoInteres;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Test unitario puro: sin Spring ni base de datos, porque lo que se verifica es la aritmética de saldos.
@ExtendWith(MockitoExtension.class)
class CuentaCorrienteServiceTest {

    @Mock
    private CuentaCorrienteRepository cuentaCorrienteRepository;
    @Mock
    private MovimientoCuentaCorrienteRepository movimientoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AbonoRepository abonoRepository;
    @Mock
    private PeriodoInteresRepository periodoInteresRepository;

    @InjectMocks
    private CuentaCorrienteService cuentaCorrienteService;

    private CuentaCorriente cuenta(String disponible, String utilizado, String limite) {
        return CuentaCorriente.builder()
                .limiteCredito(new BigDecimal(limite))
                .saldoDisponible(new BigDecimal(disponible))
                .saldoUtilizado(new BigDecimal(utilizado))
                .estado(CuentaCorriente.EstadoCuenta.ACTIVA)
                .tasaInteresMensual(new BigDecimal("0.015"))
                .build();
    }

    private void guardarDevuelveLaMismaEntidad() {
        when(cuentaCorrienteRepository.save(any(CuentaCorriente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    @DisplayName("Un cargo descuenta del disponible y el movimiento guarda los saldos previos")
    void aplicarCargo_descuentaYAudita() {
        UUID cuentaId = UUID.randomUUID();
        CuentaCorriente cc = cuenta("100000", "0", "100000");
        when(cuentaCorrienteRepository.findByIdForUpdate(cuentaId)).thenReturn(Optional.of(cc));
        guardarDevuelveLaMismaEntidad();

        cuentaCorrienteService.aplicarCargo(cuentaId, new BigDecimal("30000"), "Compra", UUID.randomUUID(), "PEDIDO");

        assertThat(cc.getSaldoUtilizado()).isEqualByComparingTo("30000");
        assertThat(cc.getSaldoDisponible()).isEqualByComparingTo("70000");

        ArgumentCaptor<MovimientoCuentaCorriente> captor = ArgumentCaptor.forClass(MovimientoCuentaCorriente.class);
        verify(movimientoRepository).save(captor.capture());
        MovimientoCuentaCorriente movimiento = captor.getValue();
        assertThat(movimiento.getSaldoAnterior()).isEqualByComparingTo("0");
        assertThat(movimiento.getSaldoNuevo()).isEqualByComparingTo("30000");
        assertThat(movimiento.getSaldoDisponibleAnterior()).isEqualByComparingTo("100000");
        assertThat(movimiento.getSaldoDisponibleNuevo()).isEqualByComparingTo("70000");
    }

    @Test
    @DisplayName("Un cargo mayor al cupo disponible se rechaza sin tocar la cuenta")
    void aplicarCargo_sinCupo_lanzaExcepcion() {
        UUID cuentaId = UUID.randomUUID();
        CuentaCorriente cc = cuenta("10000", "90000", "100000");
        when(cuentaCorrienteRepository.findByIdForUpdate(cuentaId)).thenReturn(Optional.of(cc));

        assertThatThrownBy(() -> cuentaCorrienteService.aplicarCargo(
                cuentaId, new BigDecimal("20000"), "Compra", null, null))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(cc.getSaldoUtilizado()).isEqualByComparingTo("90000");
    }

    @Test
    @DisplayName("Un abono reduce la deuda y libera cupo, sin dejar saldo a favor")
    void aplicarAbono_reduceDeuda() {
        UUID cuentaId = UUID.randomUUID();
        CuentaCorriente cc = cuenta("20000", "80000", "100000");
        when(cuentaCorrienteRepository.findByIdForUpdate(cuentaId)).thenReturn(Optional.of(cc));
        guardarDevuelveLaMismaEntidad();

        cuentaCorrienteService.aplicarAbono(cuentaId, new BigDecimal("50000"), "Pago", null, null);

        assertThat(cc.getSaldoUtilizado()).isEqualByComparingTo("30000");
        assertThat(cc.getSaldoDisponible()).isEqualByComparingTo("70000");

        // Un pago mayor a la deuda no genera saldo a favor.
        cuentaCorrienteService.aplicarAbono(cuentaId, new BigDecimal("99999"), "Pago total", null, null);
        assertThat(cc.getSaldoUtilizado()).isEqualByComparingTo("0");
        assertThat(cc.getSaldoDisponible()).isEqualByComparingTo("100000");
    }

    @Test
    @DisplayName("Reducir el límite por debajo de la deuda es inválido")
    void actualizarLimite_porDebajoDeLaDeuda_lanzaExcepcion() {
        UUID cuentaId = UUID.randomUUID();
        CuentaCorriente cc = cuenta("20000", "80000", "100000");
        when(cuentaCorrienteRepository.findByIdForUpdate(cuentaId)).thenReturn(Optional.of(cc));

        assertThatThrownBy(() -> cuentaCorrienteService.actualizarLimiteCredito(cuentaId, new BigDecimal("50000")))
                .isInstanceOf(SaldoInsuficienteException.class);
    }

    @Test
    @DisplayName("El interés mensual no se cobra dos veces en el mismo período")
    void procesarInteres_mismoPeriodo_lanzaExcepcion() {
        UUID cuentaId = UUID.randomUUID();
        CuentaCorriente cc = cuenta("80000", "20000", "100000");
        String periodo = java.time.YearMonth.now().toString();
        when(cuentaCorrienteRepository.findByIdForUpdate(cuentaId)).thenReturn(Optional.of(cc));
        when(periodoInteresRepository.findByCuentaCorrienteIdAndPeriodo(cuentaId, periodo))
                .thenReturn(Optional.of(PeriodoInteres.builder().periodo(periodo).build()));

        assertThatThrownBy(() -> cuentaCorrienteService.procesarInteresMensual(cuentaId))
                .isInstanceOf(com.ferreteria.user_service.exception.BadRequestException.class)
                .hasMessageContaining("ya fue procesado");
    }

    @Test
    @DisplayName("Una cuenta con historial financiero no se elimina físicamente")
    void eliminar_conMovimientos_rechaza() {
        UUID cuentaId = UUID.randomUUID();
        CuentaCorriente cc = cuenta("100000", "0", "100000");
        when(cuentaCorrienteRepository.findByIdForUpdate(cuentaId)).thenReturn(Optional.of(cc));
        when(movimientoRepository.countByCuentaCorrienteId(cuentaId)).thenReturn(1L);

        assertThatThrownBy(() -> cuentaCorrienteService.eliminar(cuentaId))
                .isInstanceOf(com.ferreteria.user_service.exception.ConflictException.class);
        verify(cuentaCorrienteRepository, org.mockito.Mockito.never()).deleteById(any());
    }
}
