package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.CrearLibroRequest;
import com.libros.gestion_cliente.application.service.LibroService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("libroFormController")
@RequiredArgsConstructor
public class LibroFormController {

    private final LibroService libroService;

    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    // Callback para avisar al padre que recargue la tabla
    private Runnable onSaveSuccess;

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
    }

    @FXML
    public void guardar(ActionEvent event) {
        try {
            CrearLibroRequest request = CrearLibroRequest.builder()
                    .isbn(txtIsbn.getText())
                    .titulo(txtTitulo.getText())
                    .autor(txtAutor.getText())
                    .precioBase(new BigDecimal(txtPrecio.getText()))
                    .stock(Integer.parseInt(txtStock.getText()))
                    .tematica("General") // Por ahora fijo o agrega campo
                    .build();

            libroService.crearLibro(request);

            // Cerrar ventana
            cerrarVentana();

            // Avisar para recargar tabla
            if (onSaveSuccess != null) onSaveSuccess.run();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    public void cancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtIsbn.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}