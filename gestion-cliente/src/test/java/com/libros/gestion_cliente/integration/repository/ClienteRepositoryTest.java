package com.libros.gestion_cliente.integration.repository;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Familiar;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Usar Docker, no H2
class ClienteRepositoryTest {

    // Definimos el contenedor de Postgres para el test
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("init.sql")
            .withUrlParam("stringtype", "unspecified");

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void deberiaGuardarYRecuperarClienteConFamiliares() {
        // GIVEN (Dado un cliente con un familiar)
        Familiar hijo = Familiar.builder()
                .nombre("Mateo")
                .apellido("Lopez")
                .anioNacimiento(2015)
                .relacion("Hijo")
                .build();

        Cliente cliente = Cliente.builder()
                .nombre("José")
                .apellido("Lopez")
                .dni("99999999")
                .direccion("Calle Nuñez 123")
                .build();

        cliente.addFamiliar(hijo);

        // WHEN (Cuando lo guardamos)
        Cliente guardado = clienteRepository.save(cliente);

        // THEN (Entonces debe tener ID y persistir los datos)
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getFamiliares()).hasSize(1);

        // Verificamos recuperando de la BD
        Optional<Cliente> recuperado = clienteRepository.findById(guardado.getId());
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getDni()).isEqualTo("99999999");
        assertThat(recuperado.get().getFamiliares().get(0).getNombre()).isEqualTo("Mateo");
    }

    @Test
    void deberiaDetectarSiExisteDni() {
        // GIVEN
        Cliente cliente = Cliente.builder()
                .nombre("Franco")
                .apellido("Test")
                .dni("87654321")
                .build();
        clienteRepository.save(cliente);

        // WHEN
        boolean existe = clienteRepository.existsByDni("87654321");
        boolean noExiste = clienteRepository.existsByDni("11111111");

        // THEN
        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();
    }
}