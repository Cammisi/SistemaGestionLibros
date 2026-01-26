package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.PedidoEspecialRepository;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PedidoFormController {

    private final PedidoEspecialRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TextArea txtDescripcion;

    private Runnable onSaveSuccess;

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
    }

    // --- ESTO ES LO QUE FALTABA ---
    @FXML
    public void initialize() {
        cargarClientes();
    }
    // -----------------------------

    private void cargarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        ObservableList<Cliente> listaClientes = FXCollections.observableArrayList(clientes);
        cmbCliente.setItems(listaClientes);

        cmbCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getApellido() + " " + c.getNombre();
            }

            @Override
            public Cliente fromString(String string) {
                if (string == null || string.isBlank()) return null;

                return cmbCliente.getItems().stream()
                        .filter(c -> (c.getApellido() + " " + c.getNombre()).equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    @FXML
    public void guardar(ActionEvent event) {
        if (cmbCliente.getValue() == null || txtDescripcion.getText().isBlank()) {
            NotificationUtil.show("Error", "Debe seleccionar un cliente y escribir una descripción", true, (Stage) txtDescripcion.getScene().getWindow());
            return;
        }

        try {
            PedidoEspecial pedido = PedidoEspecial.builder()
                    .cliente(cmbCliente.getValue())
                    .descripcion(txtDescripcion.getText())
                    .fechaPedido(LocalDate.now())
                    .estado(EstadoPedido.PENDIENTE)
                    .build();

            pedidoRepository.save(pedido);

            NotificationUtil.show("Éxito", "Pedido registrado", false, (Stage) txtDescripcion.getScene().getWindow());

            ((Stage) txtDescripcion.getScene().getWindow()).close();

            if (onSaveSuccess != null) onSaveSuccess.run();

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.show("Error", "Fallo al guardar: " + e.getMessage(), true, (Stage) txtDescripcion.getScene().getWindow());
        }
    }

    @FXML
    public void cancelar(ActionEvent event) {
        ((Stage) txtDescripcion.getScene().getWindow()).close();
    }
}