package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente crearCliente(CrearClienteRequest request) {
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un cliente con el DNI: " + request.getDni());
        }

        Cliente cliente = Cliente.builder()
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .direccion(request.getDireccion())
                .localidad(request.getLocalidad())
                .telefono(request.getTelefono())
                .interesesPersonales(request.getInteresesPersonales())
                .build();

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorDni(String dni) {
        return clienteRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dni));
    }
}