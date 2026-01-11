package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Familiar;
import org.junit.jupiter.api.Test;

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
}