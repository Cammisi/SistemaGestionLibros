package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_ventas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    @JsonIgnore
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id")
    private Libro libro;

    @NotNull
    @Column(name = "precio_al_momento", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAlMomento;

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Integer cantidad = 1;

    public BigDecimal getSubtotal() {
        if (precioAlMomento == null || cantidad == null) return BigDecimal.ZERO;
        return precioAlMomento.multiply(BigDecimal.valueOf(cantidad));
    }
}