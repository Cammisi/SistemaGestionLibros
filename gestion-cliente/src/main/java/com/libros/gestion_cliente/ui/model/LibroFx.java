package com.libros.gestion_cliente.ui.model;

import com.libros.gestion_cliente.domain.model.Libro;
import javafx.beans.property.*;
import lombok.Getter;

@Getter
public class LibroFx {
    private final LongProperty id;
    private final StringProperty isbn;
    private final StringProperty titulo;
    private final StringProperty autor;
    private final ObjectProperty<java.math.BigDecimal> precio;
    private final IntegerProperty stock;

    public LibroFx(Libro libro) {
        this.id = new SimpleLongProperty(libro.getId());
        this.isbn = new SimpleStringProperty(libro.getIsbn());
        this.titulo = new SimpleStringProperty(libro.getTitulo());
        this.autor = new SimpleStringProperty(libro.getAutor());
        this.precio = new SimpleObjectProperty<>(libro.getPrecioBase());
        this.stock = new SimpleIntegerProperty(libro.getStock());
    }
}