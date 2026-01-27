package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class PedidoEspecialTest {

    @Test
    void prePersist_DeberiaAsignarFechaActual_SiEsNula() {
        // GIVEN
        PedidoEspecial pedido = new PedidoEspecial();
        pedido.setFechaPedido(null); // Explicitamente nula

        // WHEN
        pedido.prePersist(); // Llamamos manualmente al ciclo de vida JPA

        // THEN
        assertThat(pedido.getFechaPedido()).isNotNull();
        assertThat(pedido.getFechaPedido()).isEqualTo(LocalDate.now());
    }

    @Test
    void prePersist_NoDeberiaSobreescribirFecha_SiYaExiste() {
        // GIVEN
        LocalDate fechaAntigua = LocalDate.of(2025, 1, 1);
        PedidoEspecial pedido = new PedidoEspecial();
        pedido.setFechaPedido(fechaAntigua);

        // WHEN
        pedido.prePersist();

        // THEN
        assertThat(pedido.getFechaPedido()).isEqualTo(fechaAntigua);
    }
}