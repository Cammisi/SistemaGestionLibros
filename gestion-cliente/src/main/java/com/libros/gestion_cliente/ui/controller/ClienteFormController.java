package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("clienteFormController")
@RequiredArgsConstructor
public class ClienteFormController {

    private final ClienteService clienteService;

    @FXML private TextField txtDni;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtLocalidad;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtIntereses;

    private Runnable onSaveSuccess;

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
    }

    @FXML
    public void guardar(ActionEvent event) {
        try {
            // Construir el DTO
            CrearClienteRequest request = CrearClienteRequest.builder()
                    .dni(txtDni.getText())
                    .nombre(txtNombre.getText())
                    .apellido(txtApellido.getText())
                    .direccion(txtDireccion.getText())
                    .localidad(txtLocalidad.getText())
                    .telefono(txtTelefono.getText())
                    .interesesPersonales(txtIntereses.getText())
                    .build();

            // Llamar al servicio
            clienteService.crearCliente(request);

            // Mostrar éxito
            NotificationUtil.show("Éxito", "Cliente registrado correctamente", false, (Stage) txtDni.getScene().getWindow());

            // Cerrar y actualizar tabla padre
            cerrarVentana();
            if (onSaveSuccess != null) onSaveSuccess.run();

        } catch (Exception e) {
            // Mostrar error (ej: DNI duplicado)
            NotificationUtil.show("Error", "No se pudo guardar: " + e.getMessage(), true, (Stage) txtDni.getScene().getWindow());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtDni.getScene().getWindow();
        stage.close();
    }
}