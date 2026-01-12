package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Familiar;
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
}