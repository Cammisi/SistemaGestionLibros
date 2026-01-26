package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Familiar;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void deberiaAgregarFamiliarCorrectamente() {
        // GIVEN
        Cliente cliente = new Cliente();
        Familiar familiar = new Familiar();
        familiar.setNombre("Hijo");

        // WHEN
        cliente.addFamiliar(familiar);

        // THEN
        assertEquals(1, cliente.getFamiliares().size());
        assertEquals(cliente, familiar.getCliente(), "La relación bidireccional debe establecerse");
    }

    @Test
    void deberiaRemoverFamiliarCorrectamente() {
        // GIVEN
        Cliente cliente = new Cliente();
        Familiar familiar = new Familiar();
        cliente.addFamiliar(familiar);

        // WHEN
        cliente.removeFamiliar(familiar);

        // THEN
        assertTrue(cliente.getFamiliares().isEmpty());
        assertNull(familiar.getCliente(), "La relación debe romperse");
    }

    @Test
    void deberiaUsarBuilder() {
        // Testeamos el Builder para asegurar cobertura
        Cliente cliente = Cliente.builder()
                .nombre("Test")
                .dni("123")
                .build();

        assertNotNull(cliente.getFechaAlta()); // Validamos el valor por defecto
        assertEquals("Test", cliente.getNombre());
    }

    @Test
    void prePersist_DeberiaAsignarFecha_SiEsNula() {
        Cliente cliente = new Cliente();
        cliente.setFechaAlta(null);

        cliente.prePersist(); // Llamada manual

        assertThat(cliente.getFechaAlta()).isNotNull();
        assertThat(cliente.getFechaAlta()).isEqualTo(LocalDate.now());
    }

    @Test
    void prePersist_NoDeberiaCambiarFecha_SiYaExiste() {
        LocalDate fechaAntigua = LocalDate.of(2000, 1, 1);
        Cliente cliente = new Cliente();
        cliente.setFechaAlta(fechaAntigua);

        cliente.prePersist();

        assertThat(cliente.getFechaAlta()).isEqualTo(fechaAntigua);
    }

    @Test
    void equals_DeberiaFuncionarCorrectamente() {
        Cliente c1 = Cliente.builder().id(1L).build();
        Cliente c2 = Cliente.builder().id(1L).build();
        Cliente c3 = Cliente.builder().id(2L).build();
        Cliente cNull = Cliente.builder().id(null).build();
        String noCliente = "Hola";

        assertThat(c1).isEqualTo(c2); // IDs iguales
        assertThat(c1).isNotEqualTo(c3); // IDs distintos
        assertThat(c1).isNotEqualTo(noCliente); // Otra clase
        assertThat(c1).isNotEqualTo(null); // Null
        assertThat(cNull).isNotEqualTo(c1); // ID Null vs ID valor
    }
}