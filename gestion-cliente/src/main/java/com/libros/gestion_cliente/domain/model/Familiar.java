package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "familiares")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Familiar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación inversa: Muchos familiares pertenecen a un Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String nombre;
    private String apellido;

    @Column(name = "anio_nacimiento")
    private Integer anioNacimiento;

    private String relacion; // Tío, Hijo, etc.

    @Column(columnDefinition = "TEXT")
    private String intereses;
}
