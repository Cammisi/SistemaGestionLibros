package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
// Importamos tu controlador visual de JavaFX
import com.libros.gestion_cliente.ui.controller.LibroController;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LibroFxControllerTest { // <-- Le cambiamos el nombre para que no choque

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private TableView<Libro> tablaLibrosMock;

    @InjectMocks
    private LibroController libroController;

    @BeforeAll
    public static void initJavaFX() {
        // Force JavaFX to run in headless mode using Monocle
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ignore if already started
        }
    }

    @BeforeEach
    public void setUp() {
        // Inyectamos la tabla simulada al controlador para evitar NullPointerException
        ReflectionTestUtils.setField(libroController, "tablaLibros", tablaLibrosMock);
    }

    @Test
    public void testSumarStock() {
        Libro libroPrueba = new Libro();
        libroPrueba.setStock(5);

        ReflectionTestUtils.invokeMethod(libroController, "sumarStock", libroPrueba);

        assertEquals(6, libroPrueba.getStock(), "El stock debió sumar 1");
        verify(libroRepository, times(1)).save(libroPrueba);
        verify(tablaLibrosMock, times(1)).refresh();
    }

    @Test
    public void testRestarStock_ConStockSuficiente() {
        Libro libroPrueba = new Libro();
        libroPrueba.setStock(5);

        ReflectionTestUtils.invokeMethod(libroController, "restarStock", libroPrueba);

        assertEquals(4, libroPrueba.getStock(), "El stock debió restar 1");
        verify(libroRepository, times(1)).save(libroPrueba);
        verify(tablaLibrosMock, times(1)).refresh();
    }
}