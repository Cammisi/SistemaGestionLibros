package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Familiar;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FamiliarTest {

    @Test
    void deberiaCrearFamiliarConBuilder() {
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Padre").build();

        // WHEN
        Familiar familiar = Familiar.builder()
                .nombre("Juan")
                .apellido("Perez")
                .anioNacimiento(2010)
                .relacion("Sobrino")
                .intereses("Comics")
                .cliente(cliente)
                .build();

        // THEN
        assertThat(familiar.getNombre()).isEqualTo("Juan");
        assertThat(familiar.getApellido()).isEqualTo("Perez");
        assertThat(familiar.getAnioNacimiento()).isEqualTo(2010);
        assertThat(familiar.getRelacion()).isEqualTo("Sobrino");
        assertThat(familiar.getIntereses()).isEqualTo("Comics");
        assertThat(familiar.getCliente()).isNotNull();
    }

    @Test
    void deberiaActualizarRelacion() {
        Familiar familiar = new Familiar();
        familiar.setRelacion("Amigo");
        assertThat(familiar.getRelacion()).isEqualTo("Amigo");
    }
}
