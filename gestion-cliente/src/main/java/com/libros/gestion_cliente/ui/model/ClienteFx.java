package com.libros.gestion_cliente.ui.model;

import com.libros.gestion_cliente.domain.model.Cliente;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.LongProperty;
import lombok.Getter;

@Getter
public class ClienteFx {
    private final LongProperty id;
    private final StringProperty dni;
    private final StringProperty nombre;
    private final StringProperty apellido;
    private final StringProperty direccion;
    private final StringProperty localidad;
    private final StringProperty telefono;

    public ClienteFx(Cliente cliente) {
        this.id = new SimpleLongProperty(cliente.getId());
        this.dni = new SimpleStringProperty(cliente.getDni());
        this.nombre = new SimpleStringProperty(cliente.getNombre());
        this.apellido = new SimpleStringProperty(cliente.getApellido());
        this.direccion = new SimpleStringProperty(cliente.getDireccion());
        this.localidad = new SimpleStringProperty(cliente.getLocalidad());
        this.telefono = new SimpleStringProperty(cliente.getTelefono());
    }

    // Método auxiliar para mostrar nombre completo en la tabla si quieres
    public StringProperty nombreCompletoProperty() {
        return new SimpleStringProperty(nombre.get() + " " + apellido.get());
    }
}