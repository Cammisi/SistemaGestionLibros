package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
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

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Usamos solo Mockito
public class LibroFxControllerTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroController libroController;

    private TableView<Libro> tablaLibrosReal;

    @BeforeAll
    public static void initJfx() throws InterruptedException {
        // Forzamos el modo Headless
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");

        // Inicializamos el Toolkit de JavaFX de forma segura y esperamos a que termine
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException e) {
            // JavaFX ya está inicializado (puede pasar si hay otros tests de FX)
        }
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            // Creamos una instancia REAL de TableView dentro del hilo de JavaFX
            tablaLibrosReal = new TableView<>();
            ReflectionTestUtils.setField(libroController, "tablaLibros", tablaLibrosReal);
            latch.countDown();
        });
        latch.await(); // Esperamos a que la instancia se asigne
    }

    @Test
    public void testSumarStock() {
        Libro libroPrueba = new Libro();
        libroPrueba.setStock(5);

        // Llamamos al método privado 'sumarStock'
        ReflectionTestUtils.invokeMethod(libroController, "sumarStock", libroPrueba);

        assertEquals(6, libroPrueba.getStock(), "El stock debió sumar 1 y llegar a 6");
        verify(libroRepository, times(1)).save(libroPrueba);
    }

    @Test
    public void testRestarStock_ConStockSuficiente() {
        Libro libroPrueba = new Libro();
        libroPrueba.setStock(5);

        ReflectionTestUtils.invokeMethod(libroController, "restarStock", libroPrueba);

        assertEquals(4, libroPrueba.getStock(), "El stock debió restar 1 y llegar a 4");
        verify(libroRepository, times(1)).save(libroPrueba);
    }
}