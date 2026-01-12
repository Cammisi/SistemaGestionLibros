package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "libros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El ISBN es obligatorio")
    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 100)
    private String autor;

    @Column(name = "tematica", length = 100)
    private String tematica;

    // SQL: cant_volumenes INT DEFAULT 1
    @Column(name = "cant_volumenes")
    @Builder.Default
    private Integer cantVolumenes = 1;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "precio_base", precision = 10, scale = 2)
    private BigDecimal precioBase; // Antes precio

    // SQL: stock INT DEFAULT 0
    @Min(0)
    @Builder.Default
    private Integer stock = 0;

}