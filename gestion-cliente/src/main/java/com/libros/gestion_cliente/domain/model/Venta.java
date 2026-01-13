package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate; // Usamos LocalDate porque en SQL es DATE
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: nro_factura VARCHAR(50) UNIQUE
    @NotNull(message = "El número de factura es obligatorio")
    @Column(name = "nro_factura", unique = true, nullable = false, length = 50)
    private String nroFactura;

    // SQL: cantidad_cuotas INT DEFAULT 1
    @Min(value = 1, message = "Debe haber al menos 1 cuota")
    @Column(name = "cantidad_cuotas")
    @Builder.Default
    private Integer cantidadCuotas = 1;

    // SQL: estado estado_venta DEFAULT 'EN_PROCESO'
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "estado_venta", nullable = false) // Postgres Enum
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.EN_PROCESO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // SQL: fecha_venta DATE DEFAULT CURRENT_DATE
    @Column(name = "fecha_venta", nullable = false)
    @Builder.Default
    private LocalDate fechaVenta = LocalDate.now();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVenta> detalles = new ArrayList<>();

    // SQL: monto_total DECIMAL(10, 2) NOT NULL
    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoTotal = BigDecimal.ZERO;

    // --- Helpers ---
    public void addDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
        recalcularTotal();
    }

    public void removeDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.montoTotal = detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}