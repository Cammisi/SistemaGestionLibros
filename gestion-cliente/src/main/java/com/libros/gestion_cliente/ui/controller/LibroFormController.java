package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.CrearLibroRequest;
import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtTematica;
    @FXML private TextField txtVolumenes;

    @FXML
    public void guardarLibro(ActionEvent event) {
        try {
            if (txtTitulo.getText().isBlank() || txtPrecio.getText().isBlank()) {
                NotificationUtil.show("Error", "Título y Precio son obligatorios", true, (Stage) txtTitulo.getScene().getWindow());
                return;
            }

            CrearLibroRequest request = CrearLibroRequest.builder()
                    .titulo(txtTitulo.getText())
                    .autor(txtAutor.getText())
                    .isbn(txtIsbn.getText())
                    .precioBase(new BigDecimal(txtPrecio.getText()))
                    .stock(Integer.parseInt(txtStock.getText()))
                    .tematica(txtTematica.getText())
                    .cantVolumenes(txtVolumenes.getText().isEmpty() ? 1 : Integer.parseInt(txtVolumenes.getText()))
                    .build();

            libroService.crearLibro(request);

            NotificationUtil.show("Éxito", "Libro guardado correctamente", false, (Stage) txtTitulo.getScene().getWindow());

            // Volver a la lista automáticamente
            cancelar(null);

        } catch (NumberFormatException e) {
            NotificationUtil.show("Error", "Precio, Stock y Volúmenes deben ser números", true, (Stage) txtTitulo.getScene().getWindow());
        } catch (Exception e) {
            NotificationUtil.show("Error", "Fallo al guardar: " + e.getMessage(), true, (Stage) txtTitulo.getScene().getWindow());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/libros.fxml")); // Volver a la lista
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) txtTitulo.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}