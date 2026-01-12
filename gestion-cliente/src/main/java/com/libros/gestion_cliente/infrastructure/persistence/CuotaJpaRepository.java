package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuotaJpaRepository extends JpaRepository<Cuota, Long>, CuotaRepository {
}