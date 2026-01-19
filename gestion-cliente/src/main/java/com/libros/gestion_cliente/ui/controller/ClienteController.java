package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.ui.model.ClienteFx;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
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

@Component("clienteFxController") // Nombre único para evitar conflictos
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ApplicationContext applicationContext;

    @FXML private BorderPane rootPane;
    @FXML private TableView<ClienteFx> tablaClientes;
    @FXML private TableColumn<ClienteFx, String> colDni;
    @FXML private TableColumn<ClienteFx, String> colNombre;
    @FXML private TableColumn<ClienteFx, String> colLocalidad;
    @FXML private TableColumn<ClienteFx, String> colDireccion;
    @FXML private TableColumn<ClienteFx, String> colTelefono;
    @FXML private Label lblPagina;
    @FXML private ProgressIndicator spinnerCarga;

    private int paginaActual = 0;
    private final int TAMANO_PAGINA = 15;
    private int totalPaginas = 0;

    @FXML
    public void initialize() {
        // Configurar columnas
        colDni.setCellValueFactory(cell -> cell.getValue().getDni());
        // Concatenamos nombre y apellido para mostrarlo junto
        colNombre.setCellValueFactory(cell -> cell.getValue().nombreCompletoProperty());
        colLocalidad.setCellValueFactory(cell -> cell.getValue().getLocalidad());
        colDireccion.setCellValueFactory(cell -> cell.getValue().getDireccion());
        colTelefono.setCellValueFactory(cell -> cell.getValue().getTelefono());

        // Transición de entrada
        rootPane.setOpacity(0);
        FadeTransition fade = new FadeTransition(javafx.util.Duration.millis(600), rootPane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        cargarClientes();
    }

    private void cargarClientes() {
        spinnerCarga.setVisible(true);
        tablaClientes.setDisable(true);

        Task<Page<Cliente>> task = new Task<>() {
            @Override
            protected Page<Cliente> call() throws Exception {
                // Thread.sleep(300); // Opcional: para ver el spinner
                return clienteService.listarClientes(PageRequest.of(paginaActual, TAMANO_PAGINA));
            }
        };

        task.setOnSucceeded(e -> {
            Page<Cliente> pagina = task.getValue();
            this.totalPaginas = pagina.getTotalPages();

            var clientesFx = pagina.getContent().stream()
                    .map(ClienteFx::new)
                    .toList();

            tablaClientes.setItems(FXCollections.observableArrayList(clientesFx));

            lblPagina.setText("Página " + (paginaActual + 1) + " de " + Math.max(1, totalPaginas));

            spinnerCarga.setVisible(false);
            tablaClientes.setDisable(false);
        });

        task.setOnFailed(e -> {
            spinnerCarga.setVisible(false);
            tablaClientes.setDisable(false);
            e.getSource().getException().printStackTrace();
            mostrarNotificacion("Error", "No se pudieron cargar los clientes", true);
        });

        new Thread(task).start();
    }

    @FXML
    public void agregarCliente(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Configurar el callback para recargar la tabla al guardar
            ClienteFormController controller = loader.getController();
            controller.setOnSaveSuccess(this::cargarClientes);

            // Crear ventana modal
            Stage modalStage = new Stage();
            modalStage.setTitle("Nuevo Cliente");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(javafx.stage.Modality.WINDOW_MODAL); // Bloquea la ventana de atrás
            modalStage.initOwner(tablaClientes.getScene().getWindow()); // Asigna padre
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarNotificacion("Error", "No se pudo abrir el formulario", true);
        }
    }

    @FXML
    public void paginaAnterior(ActionEvent event) {
        if (paginaActual > 0) {
            paginaActual--;
            cargarClientes();
        }
    }

    @FXML
    public void paginaSiguiente(ActionEvent event) {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            cargarClientes();
        }
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Usamos setRoot para mantener el tamaño de la ventana
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Fernando Libros - Sistema de Gestión");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarNotificacion(String titulo, String mensaje, boolean isError) {
        if (rootPane != null && rootPane.getScene() != null) {
            NotificationUtil.show(titulo, mensaje, isError, (Stage) rootPane.getScene().getWindow());
        }
    }
}