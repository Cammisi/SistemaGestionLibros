package com.libros.gestion_cliente.ui.model;

import com.libros.gestion_cliente.domain.model.Venta;
import javafx.beans.property.*;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class VentaFx {
    private final LongProperty id;
    private final StringProperty nroFactura;
    private final StringProperty fecha;
    private final StringProperty clienteNombre;
    private final StringProperty montoTotal;
    private final StringProperty estado;

    // Formateador de fecha
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public VentaFx(Venta venta) {
        this.id = new SimpleLongProperty(venta.getId());
        this.nroFactura = new SimpleStringProperty(venta.getNroFactura());
        this.fecha = new SimpleStringProperty(venta.getFechaVenta().format(FORMATO_FECHA));
        this.clienteNombre = new SimpleStringProperty(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido());
        this.montoTotal = new SimpleStringProperty("$ " + venta.getMontoTotal().toString());
        this.estado = new SimpleStringProperty(venta.getEstado().name());
    }
}