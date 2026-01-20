package com.libros.gestion_cliente.ui.model;

import com.libros.gestion_cliente.domain.model.Libro;
import javafx.beans.property.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ItemCarritoFx {
    private final LongProperty libroId;
    private final StringProperty titulo;
    private final ObjectProperty<BigDecimal> precioUnitario;
    private final IntegerProperty cantidad;
    private final ObjectProperty<BigDecimal> subtotal;

    public ItemCarritoFx(Libro libro, int cantidad) {
        this.libroId = new SimpleLongProperty(libro.getId());
        this.titulo = new SimpleStringProperty(libro.getTitulo());
        this.precioUnitario = new SimpleObjectProperty<>(libro.getPrecioBase());
        this.cantidad = new SimpleIntegerProperty(cantidad);

        // Calculamos subtotal inicial
        this.subtotal = new SimpleObjectProperty<>(
                libro.getPrecioBase().multiply(BigDecimal.valueOf(cantidad))
        );

        // Listener: Si cambia la cantidad, recalcular subtotal automáticamente
        this.cantidad.addListener((obs, oldVal, newVal) -> {
            this.subtotal.set(
                    this.precioUnitario.get().multiply(BigDecimal.valueOf(newVal.intValue()))
            );
        });
    }
}