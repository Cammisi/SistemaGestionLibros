package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.application.dto.CrearFamiliarRequest;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Familiar;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.FamiliarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamiliarService {

    private final FamiliarRepository familiarRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public Familiar agregarFamiliar(CrearFamiliarRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente titular no encontrado"));

        Familiar familiar = Familiar.builder()
                .cliente(cliente)
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .relacion(request.getRelacion())
                .anioNacimiento(request.getAnioNacimiento())
                .intereses(request.getIntereses())
                .build();

        return familiarRepository.save(familiar);
    }

    @Transactional(readOnly = true)
    public List<Familiar> listarPorCliente(Long clienteId) {
        // Validamos que el cliente exista para dar un error más claro si no
        if (clienteRepository.findById(clienteId).isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        return familiarRepository.findByClienteId(clienteId);
    }
}