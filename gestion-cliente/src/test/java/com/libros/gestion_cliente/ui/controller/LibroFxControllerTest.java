package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// ¡NUEVO! Le decimos a JUnit que esta es una prueba de aplicación JavaFX
@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class LibroFxControllerTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private TableView<Libro> tablaLibrosMock;

    @InjectMocks
    private LibroController libroController;

    @BeforeAll
    public static void setupHeadlessMode() {
        // Configuramos TestFX para que corra en modo servidor/consola (Headless)
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    public void setUp() {
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