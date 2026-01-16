package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente crearCliente(CrearClienteRequest request) {
        // Regla: No duplicar DNI
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un cliente con el DNI: " + request.getDni());
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .dni(request.getDni())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .localidad(request.getLocalidad())
                .interesesPersonales(request.getInteresesPersonales())
                .fechaAlta(LocalDate.now()) // Fecha automática
                .build();

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorDni(String dni) {
        return clienteRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dni));
    }
}