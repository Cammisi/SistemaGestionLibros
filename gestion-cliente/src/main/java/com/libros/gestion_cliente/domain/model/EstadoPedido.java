package com.libros.gestion_cliente.domain.model;

public enum EstadoPedido {
    PENDIENTE,  // Recién creado
    DISPONIBLE, // Llegó a la librería
    ENTREGADO,  // Se lo llevó el cliente
    CANCELADO   // El cliente se arrepintió
}