package com.libros.gestion_cliente.integration.repository;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Familiar;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.FamiliarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FamiliarRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private FamiliarRepository familiarRepository;

    @Autowired
    private ClienteRepository clienteRepository; // Necesitamos esto para crear al padre primero

    @Test
    void deberiaGuardarYBuscarFamiliarPorCliente() {
        // GIVEN: Creamos y guardamos un cliente (el padre)
        Cliente padre = Cliente.builder()
                .nombre("Leandro")
                .apellido("Gomez")
                .dni("11111111")
                .build();
        clienteRepository.save(padre); // Guardamos para tener ID

        // Creamos el familiar y lo vinculamos
        Familiar hijo = Familiar.builder()
                .nombre("Francisco")
                .apellido("Gomez")
                .relacion("Hijo")
                .cliente(padre) // Vinculación manual para este test
                .build();

        // WHEN
        familiarRepository.save(hijo);

        // THEN
        List<Familiar> familiares = familiarRepository.findByClienteId(padre.getId());
        assertThat(familiares).hasSize(1);
        assertThat(familiares.get(0).getNombre()).isEqualTo("Francisco");
        assertThat(familiares.get(0).getCliente().getDni()).isEqualTo("11111111");
    }
}