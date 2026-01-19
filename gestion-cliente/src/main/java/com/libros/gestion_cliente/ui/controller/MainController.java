package com.libros.gestion_cliente.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Inyecta el ApplicationContext automáticamente
public class MainController {

    private final ApplicationContext applicationContext;

    @FXML
    private VBox rootPane;

    @FXML
    public void initialize() {
        // Quitamos el foco del primer botón para que se vea limpio al arrancar
        if (rootPane != null) {
            javafx.application.Platform.runLater(() -> rootPane.requestFocus());
        }
    }

    @FXML
    public void irAClientes(ActionEvent event) {
        navegar(event, "/fxml/clientes.fxml", "Cartera de Clientes");
    }

    @FXML
    public void irALibros(ActionEvent event) {
        navegar(event, "/fxml/libros.fxml", "Gestión de Libros");
    }

    @FXML
    public void irAVentas(ActionEvent event) {
        System.out.println("Navegando a Ventas... (Pantalla aún no creada)");
    }

    @FXML
    public void irAEstrategia(ActionEvent event) {
        System.out.println("Navegando a Estrategia... (Pantalla aún no creada)");
    }

    // --- MÉTODO REUTILIZABLE PARA NAVEGAR ---
    private void navegar(ActionEvent event, String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Obtenemos la escena actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // CAMBIO CLAVE: Reemplazamos el contenido, no la escena completa
            scene.setRoot(root);

            stage.setTitle("Fernando Libros - " + titulo);
            // Ya no hacemos stage.show() porque ya está visible

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}