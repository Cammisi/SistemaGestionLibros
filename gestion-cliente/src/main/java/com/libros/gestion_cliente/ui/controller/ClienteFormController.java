package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.application.dto.CrearFamiliarRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.application.service.FamiliarService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("clienteFormController")
@RequiredArgsConstructor
public class ClienteFormController {

    private final ClienteService clienteService;
    private final FamiliarService familiarService;

    // Campos Cliente
    @FXML private TextField txtDni;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtLocalidad;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtIntereses;
    @FXML private DatePicker dpFechaAlta;

    // Campos Familiar (NUEVOS AGREGADOS)
    @FXML private TextField txtFamNombre;
    @FXML private TextField txtFamApellido; // <--- Nuevo
    @FXML private TextField txtFamAnio;
    @FXML private ComboBox<String> cmbFamRelacion;
    @FXML private TextField txtFamIntereses; // <--- Nuevo

    // Tabla
    @FXML private TableView<CrearFamiliarRequest> tablaFamiliares;
    @FXML private TableColumn<CrearFamiliarRequest, String> colFamNombre;
    @FXML private TableColumn<CrearFamiliarRequest, String> colFamApellido; // <--- Nuevo
    @FXML private TableColumn<CrearFamiliarRequest, String> colFamRelacion;
    @FXML private TableColumn<CrearFamiliarRequest, Integer> colFamAnio;
    @FXML private TableColumn<CrearFamiliarRequest, Integer> colFamEdad;
    @FXML private TableColumn<CrearFamiliarRequest, String> colFamIntereses; // <--- Nuevo

    private final ObservableList<CrearFamiliarRequest> listaFamiliares = FXCollections.observableArrayList();
    private Runnable onSaveSuccess;

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
    }

    @FXML
    public void initialize() {
        dpFechaAlta.setValue(LocalDate.now());
        cmbFamRelacion.setItems(FXCollections.observableArrayList(
                "Hijo/a", "Nieto/a", "Sobrino/a", "Ahijado/a",
                "Esposo/a", "Hermano/a", "Cuñado/a", "Padre",
                "Madre", "Abuelo/a", "Tio/a", "Padrino", "Madrina"
        ));

        configurarTablaFamilia();
    }

    private void configurarTablaFamilia() {
        tablaFamiliares.setItems(listaFamiliares);
        colFamNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colFamApellido.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApellido())); // <--- Nuevo
        colFamRelacion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRelacion()));
        colFamAnio.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getAnioNacimiento()));
        colFamIntereses.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIntereses())); // <--- Nuevo

        // Calculamos la edad visualmente
        colFamEdad.setCellValueFactory(c -> {
            int edad = LocalDate.now().getYear() - c.getValue().getAnioNacimiento();
            return new SimpleObjectProperty<>(edad);
        });
    }

    @FXML
    public void agregarFamiliarTabla(ActionEvent event) {
        try {
            if (txtFamNombre.getText().isBlank() || txtFamAnio.getText().isBlank()) return;

            int anio = Integer.parseInt(txtFamAnio.getText());
            String relacion = cmbFamRelacion.getValue() != null ? cmbFamRelacion.getValue() : "Hijo/a";
            // Si el apellido familiar está vacío, sugerimos usar el del cliente por defecto, o dejar vacío.
            String apellidoFam = txtFamApellido.getText().isBlank() ? txtApellido.getText() : txtFamApellido.getText();

            listaFamiliares.add(CrearFamiliarRequest.builder()
                    .nombre(txtFamNombre.getText())
                    .apellido(apellidoFam) // <--- Guardamos apellido
                    .anioNacimiento(anio)
                    .relacion(relacion)
                    .intereses(txtFamIntereses.getText()) // <--- Guardamos intereses
                    .build());

            // Limpiar campos para el siguiente
            txtFamNombre.clear();
            txtFamApellido.clear();
            txtFamAnio.clear();
            txtFamIntereses.clear();
            txtFamNombre.requestFocus();

        } catch (NumberFormatException e) {
            NotificationUtil.show("Error", "El año debe ser un número (ej: 2015)", true, (Stage) txtDni.getScene().getWindow());
        }
    }

    @FXML
    public void guardar(ActionEvent event) {
        try {
            // 1. Guardar Cliente
            CrearClienteRequest request = CrearClienteRequest.builder()
                    .dni(txtDni.getText())
                    .nombre(txtNombre.getText())
                    .apellido(txtApellido.getText())
                    .direccion(txtDireccion.getText())
                    .localidad(txtLocalidad.getText())
                    .telefono(txtTelefono.getText())
                    .interesesPersonales(txtIntereses.getText())
                    .fechaAlta(dpFechaAlta.getValue())
                    .build();

            Cliente clienteGuardado = clienteService.crearCliente(request);

            // 2. Guardar Familiares vinculados
            for (CrearFamiliarRequest familiarReq : listaFamiliares) {
                familiarReq.setClienteId(clienteGuardado.getId());
                familiarService.agregarFamiliar(familiarReq);
            }

            NotificationUtil.show("Éxito", "Cliente y familia registrados", false, (Stage) txtDni.getScene().getWindow());
            cerrarVentana();
            if (onSaveSuccess != null) onSaveSuccess.run();

        } catch (Exception e) {
            NotificationUtil.show("Error", "Error al guardar: " + e.getMessage(), true, (Stage) txtDni.getScene().getWindow());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) txtDni.getScene().getWindow()).close();
    }
}