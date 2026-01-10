package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 20)
    @Column(unique = true, length = 20)
    private String dni;

    private String direccion;
    private String telefono;
    private String localidad;

    @Column(name = "intereses_personales", columnDefinition = "TEXT")
    private String interesesPersonales;

    @Column(name = "fecha_alta")
    @Builder.Default
    private LocalDate fechaAlta = LocalDate.now();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Familiar> familiares = new ArrayList<>();

    public void addFamiliar(Familiar familiar) {
        familiares.add(familiar);
        familiar.setCliente(this);
    }

    public void removeFamiliar(Familiar familiar) {
        familiares.remove(familiar);
        familiar.setCliente(null);
    }
}