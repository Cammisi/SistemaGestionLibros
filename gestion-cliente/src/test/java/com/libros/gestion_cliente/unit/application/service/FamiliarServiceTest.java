package com.libros.gestion_cliente.unit.application.service;

import com.libros.gestion_cliente.application.dto.CrearFamiliarRequest;
import com.libros.gestion_cliente.application.service.FamiliarService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Familiar;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.FamiliarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamiliarServiceTest {

    @Mock private FamiliarRepository familiarRepository;
    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private FamiliarService familiarService;

    // --- 1. Agregar Familiar ---
    @Test
    void agregarFamiliar_DeberiaGuardar_CuandoClienteExiste() {
        CrearFamiliarRequest request = new CrearFamiliarRequest();
        request.setClienteId(1L);
        request.setNombre("Hijo");
        request.setRelacion("Hijo");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(new Cliente()));
        when(familiarRepository.save(any(Familiar.class))).thenAnswer(i -> i.getArgument(0));

        Familiar resultado = familiarService.agregarFamiliar(request);

        assertThat(resultado.getNombre()).isEqualTo("Hijo");
        verify(familiarRepository).save(any(Familiar.class));
    }

    @Test
    void agregarFamiliar_DeberiaLanzarExcepcion_CuandoClienteNoExiste() {
        CrearFamiliarRequest request = new CrearFamiliarRequest();
        request.setClienteId(99L);

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> familiarService.agregarFamiliar(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente titular no encontrado");
    }

    // --- 2. Listar por Cliente ---
    @Test
    void listarPorCliente_DeberiaRetornarLista_CuandoClienteExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(new Cliente()));
        when(familiarRepository.findByClienteId(1L)).thenReturn(List.of(new Familiar()));

        List<Familiar> lista = familiarService.listarPorCliente(1L);

        assertThat(lista).hasSize(1);
    }

    @Test
    void listarPorCliente_DeberiaLanzarExcepcion_CuandoClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> familiarService.listarPorCliente(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente no encontrado");
    }
}