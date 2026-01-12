package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import com.libros.gestion_cliente.domain.repository.PedidoEspecialRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoEspecialJpaRepository extends JpaRepository<PedidoEspecial, Long>, PedidoEspecialRepository {
}