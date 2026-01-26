package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Familiar;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FamiliarRepository {
    Familiar save(Familiar familiar);
    <S extends Familiar> List<S> saveAll(Iterable<S> familiares); // Agregado para evitar conflictos
    Optional<Familiar> findById(Long id);
    List<Familiar> findAll();
    void deleteById(Long id);

    // Métodos específicos útiles
    List<Familiar> findByClienteId(Long clienteId);
    List<Familiar> findByApellido(String apellido);

    // Lógica Inversa:
    // Para buscar niños de 5 a 10 años, busco nacidos entre (AñoActual-10) y (AñoActual-5).
    @Query("SELECT f FROM Familiar f JOIN FETCH f.cliente c WHERE f.anioNacimiento BETWEEN :anioInicio AND :anioFin")
    List<Familiar> buscarPorRangoAnio(int anioInicio, int anioFin);
}