package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.CrearLibroRequest;
import com.libros.gestion_cliente.application.service.LibroService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LibroFormController {

    private final LibroService libroService;
    private final ApplicationContext applicationContext;

    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtTematica; // Asegúrate de que este esté en el FXML
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    @FXML
    public void guardar(ActionEvent event) {
        try {
            CrearLibroRequest request = CrearLibroRequest.builder()
                    .isbn(txtIsbn.getText())
                    .titulo(txtTitulo.getText())
                    .autor(txtAutor.getText())
                    .tematica(txtTematica.getText()) // Agregado
                    .precioBase(new BigDecimal(txtPrecio.getText()))
                    .stock(Integer.parseInt(txtStock.getText()))
                    .build();

            libroService.crearLibro(request);
            mostrarAlerta("Éxito", "Libro guardado correctamente");

            // Volver a la lista automáticamente tras guardar
            cancelar(event);

        } catch (Exception e) {
            mostrarAlerta("Error", "Datos inválidos: " + e.getMessage());
        }
    }

    // --- CORRECCIÓN: ESTE MÉTODO EVITA QUE LA APP SE CIERRE ---
    @FXML
    public void cancelar(ActionEvent event) {
        try {
            // Cargamos la vista de la lista (libros.fxml) en lugar de salir
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/libros.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Obtenemos el Stage desde el botón que disparó el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}