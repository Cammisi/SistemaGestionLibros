package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "pedidos_especiales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class PedidoEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(length = 200)
    private String descripcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "estado_pedido", nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
}