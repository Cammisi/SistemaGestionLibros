package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.ui.model.LibroFx;
import com.libros.gestion_cliente.ui.util.NotificationUtil; // Asegúrate de importar esto
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component("libroFxController")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;
    private final ApplicationContext applicationContext; // Para navegar

    @FXML private BorderPane rootPane;
    @FXML private TableView<LibroFx> tablaLibros;
    @FXML private TableColumn<LibroFx, String> colIsbn;
    @FXML private TableColumn<LibroFx, String> colTitulo;
    @FXML private TableColumn<LibroFx, String> colAutor;
    @FXML private TableColumn<LibroFx, BigDecimal> colPrecio;
    @FXML private TableColumn<LibroFx, Integer> colStock;
    @FXML private Label lblPagina;

    // Inyectar del FXML el spinner
    @FXML private ProgressIndicator spinnerCarga;

    private int paginaActual = 0;
    private final int TAMANO_PAGINA = 15;
    private int totalPaginas = 0;

    @FXML
    public void initialize() {
        // Configurar columnas
        colIsbn.setCellValueFactory(cellData -> cellData.getValue().getIsbn());
        colTitulo.setCellValueFactory(cellData -> cellData.getValue().getTitulo());
        colAutor.setCellValueFactory(cellData -> cellData.getValue().getAutor());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().getPrecio());
        colStock.setCellValueFactory(cellData -> cellData.getValue().getStock().asObject());

        // Efectos de transición al entrar
        rootPane.setOpacity(0);
        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(600), rootPane);
        fadeOut.setFromValue(0);
        fadeOut.setToValue(1);
        fadeOut.play();

        // Cargar datos
        cargarLibros();
    }

    private void cargarLibros() {
        // 1. Mostrar Spinner
        spinnerCarga.setVisible(true);
        tablaLibros.setDisable(true); // Evitar clics mientras carga

        // 2. Crear Tarea en Segundo Plano (Background Thread)
        Task<Page<Libro>> task = new Task<>() {
            @Override
            protected Page<Libro> call() throws Exception {
                // Esto ocurre en otro hilo, no congela la UI
                // Simulo retardo para que veas el efecto (borrar en prod si se desea)
                Thread.sleep(500);
                return libroService.listarLibros(PageRequest.of(paginaActual, TAMANO_PAGINA));
            }
        };

        // 3. Cuando termina la tarea (Éxito)
        task.setOnSucceeded(e -> {
            Page<Libro> pagina = task.getValue();
            this.totalPaginas = pagina.getTotalPages();

            var librosFx = pagina.getContent().stream()
                    .map(LibroFx::new)
                    .toList();

            tablaLibros.setItems(FXCollections.observableArrayList(librosFx));

            // Ajuste visual para el texto de paginación
            lblPagina.setText("Página " + (paginaActual + 1) + " de " + Math.max(1, totalPaginas));

            // Ocultar spinner
            spinnerCarga.setVisible(false);
            tablaLibros.setDisable(false);
        });

        // 4. Si falla
        task.setOnFailed(e -> {
            spinnerCarga.setVisible(false);
            tablaLibros.setDisable(false);
            e.getSource().getException().printStackTrace();
            mostrarNotificacion("Error", "No se pudieron cargar los libros", true);
        });

        // 5. Arrancar el hilo
        new Thread(task).start();
    }

    @FXML
    public void agregarLibro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/libro_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Configurar el callback para recargar la tabla al guardar
            LibroFormController controller = loader.getController();
            controller.setOnSaveSuccess(this::cargarLibros);

            // Crear ventana modal
            Stage modalStage = new Stage();
            modalStage.setTitle("Nuevo Libro");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(javafx.stage.Modality.WINDOW_MODAL); // Bloquea la ventana de atrás
            modalStage.initOwner(tablaLibros.getScene().getWindow()); // Asigna padre
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void paginaAnterior(ActionEvent event) {
        if (paginaActual > 0) {
            paginaActual--;
            cargarLibros();
        }
    }

    @FXML
    public void paginaSiguiente(ActionEvent event) {
        if (paginaActual < totalPaginas - 1) { // <--- SOLO AVANZA SI NO ES LA ÚLTIMA
            paginaActual++;
            cargarLibros();
        }
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = stage.getScene();

            scene.setRoot(root);

            stage.setTitle("Fernando Libros - Sistema de Gestión");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para usar NotificationUtil
    private void mostrarNotificacion(String titulo, String mensaje, boolean isError) {
        if (rootPane != null && rootPane.getScene() != null) {
            NotificationUtil.show(titulo, mensaje, isError, (Stage) rootPane.getScene().getWindow());
        }
    }
}