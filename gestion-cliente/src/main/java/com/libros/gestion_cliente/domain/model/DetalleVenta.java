package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_ventas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la Venta (Cabecera)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    // Relación con el Libro (Producto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidad;

    // Guardamos el precio histórico (snapshot) al momento de la venta
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Método auxiliar para calcular subtotal (Cantidad * Precio)
    public BigDecimal getSubtotal() {
        if (precioUnitario == null || cantidad == null) return BigDecimal.ZERO;
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}