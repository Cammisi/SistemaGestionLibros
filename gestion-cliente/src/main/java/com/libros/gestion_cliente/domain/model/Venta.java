package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- NUEVOS CAMPOS ---

    @NotNull(message = "El número de factura es obligatorio")
    @Column(name = "nro_factura", unique = true, nullable = false)
    private Integer nroFactura;

    @Min(value = 1, message = "Debe haber al menos 1 cuota")
    @Column(name = "cantidad_cuotas")
    @Builder.Default
    private Integer cantidadCuotas = 1;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING) // Guarda "PAGANDO" o "FINALIZADA" en la BD
    @Column(nullable = false)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.FINALIZADA;

    // ---------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVenta> detalles = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    // --- Métodos Helper ---

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
        this.total = detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}