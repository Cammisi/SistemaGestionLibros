package com.libros.gestion_cliente.unit.application.service;

import com.libros.gestion_cliente.application.service.CuotaService;
import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import com.libros.gestion_cliente.domain.model.EstadoVenta;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuotaServiceTest {

    @Mock private CuotaRepository cuotaRepository;
    @Mock private VentaRepository ventaRepository;
    @InjectMocks private CuotaService cuotaService;

    @Test
    void registrarPago_DeberiaMarcarComoPagada_YDejarVentaEnProceso_SiQuedanCuotas() {
        // GIVEN: Una venta con ID 1 y una cuota pendiente
        Venta venta = Venta.builder().id(1L).estado(EstadoVenta.EN_PROCESO).build();
        Cuota cuota = Cuota.builder().id(10L).estado(EstadoCuota.PENDIENTE).venta(venta).build();

        when(cuotaRepository.findById(10L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        // Simulamos que AÚN quedan cuotas pendientes (retorna 1)
        when(cuotaRepository.countByVentaIdAndEstado(1L, EstadoCuota.PENDIENTE)).thenReturn(1L);

        // WHEN
        Cuota resultado = cuotaService.registrarPago(10L);

        // THEN
        assertThat(resultado.getEstado()).isEqualTo(EstadoCuota.PAGADA);
        assertThat(resultado.getFechaPagoReal()).isNotNull();
        // La venta NO debe haber cambiado
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void registrarPago_DeberiaFinalizarVenta_SiEraLaUltimaCuota() {
        // GIVEN
        Venta venta = Venta.builder().id(1L).estado(EstadoVenta.EN_PROCESO).build();
        Cuota cuota = Cuota.builder().id(10L).estado(EstadoCuota.PENDIENTE).venta(venta).build();

        when(cuotaRepository.findById(10L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        // Simulamos que YA NO quedan pendientes (retorna 0)
        when(cuotaRepository.countByVentaIdAndEstado(1L, EstadoCuota.PENDIENTE)).thenReturn(0L);

        // WHEN
        cuotaService.registrarPago(10L);

        // THEN
        // Verificamos que se guardó la venta con estado FINALIZADA
        verify(ventaRepository).save(argThat(v -> v.getEstado() == EstadoVenta.FINALIZADA));
    }

    @Test
    void registrarPago_DeberiaLanzarExcepcion_SiYaEstabaPagada() {
        Cuota cuota = Cuota.builder().id(10L).estado(EstadoCuota.PAGADA).build();
        when(cuotaRepository.findById(10L)).thenReturn(Optional.of(cuota));

        assertThatThrownBy(() -> cuotaService.registrarPago(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está pagada");
    }

    @Test
    void registrarPago_DeberiaLanzarExcepcion_SiNoExiste() {
        when(cuotaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cuotaService.registrarPago(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void buscarPorId_DeberiaLanzarExcepcion_CuandoNoExiste() {
        // GIVEN
        when(cuotaRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> cuotaService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cuota no encontrada con ID: 99");
    }

    @Test
    void buscarPorId_DeberiaLanzarExcepcion_CuandoIdNoExiste() {
        // GIVEN
        Long id = 99L;
        when(cuotaRepository.findById(id)).thenReturn(Optional.empty());

        // WHEN & THEN
        // Usamos assertThatThrownBy para asegurar que se instancia la excepción
        assertThatThrownBy(() -> cuotaService.buscarPorId(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cuota no encontrada con ID: " + id);
    }
}