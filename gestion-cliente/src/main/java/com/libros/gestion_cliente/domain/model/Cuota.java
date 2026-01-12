package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cuotas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @NotNull
    @Min(1)
    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "monto_cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoCuota;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago_real")
    private LocalDate fechaPagoReal;

    @Column(name = "nro_recibo_pago", length = 50)
    private String nroReciboPago;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "estado_cuota", nullable = false)
    @Builder.Default
    private EstadoCuota estado = EstadoCuota.PENDIENTE;
}