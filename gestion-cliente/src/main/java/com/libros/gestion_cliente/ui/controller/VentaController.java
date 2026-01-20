package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.CrearDetalleVentaRequest;
import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.application.service.VentaService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component("ventaFxController")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final LibroService libroService;
    private final ClienteService clienteService;
    private final ApplicationContext applicationContext;

    @FXML private BorderPane rootPane;
    @FXML private ComboBox<Cliente> cmbClientes;
    @FXML private ComboBox<Libro> cmbLibros; // Ahora es un ComboBox simple

    @FXML private TableView<ItemCarrito> tablaCarrito;
    @FXML private TableColumn<ItemCarrito, String> colCarritoTitulo;
    @FXML private TableColumn<ItemCarrito, Integer> colCarritoCantidad;
    @FXML private TableColumn<ItemCarrito, BigDecimal> colCarritoSubtotal;
    @FXML private TableColumn<ItemCarrito, Void> colCarritoEliminar;

    @FXML private ComboBox<Integer> cmbCuotas;
    @FXML private Label lblTotal;

    private final ObservableList<ItemCarrito> itemsCarrito = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTablaCarrito();
        cargarClientes();
        cargarCuotas();
        cargarLibrosCombo();
        tablaCarrito.setItems(itemsCarrito);
    }

    private void cargarLibrosCombo() {
        // Carga simple: Traer datos y ponerlos en el combo
        List<Libro> libros = libroService.listarLibros(PageRequest.of(0, 2000)).getContent();
        cmbLibros.setItems(FXCollections.observableArrayList(libros));

        // Converter simple: Solo para que se vea el Título en la lista
        cmbLibros.setConverter(new StringConverter<Libro>() {
            @Override
            public String toString(Libro l) {
                return (l == null) ? "" : l.getTitulo();
            }

            @Override
            public Libro fromString(String string) {
                return null; // No editable, no necesitamos conversión inversa
            }
        });
    }

    @FXML
    public void agregarDesdeCombo(ActionEvent event) {
        Libro libroSeleccionado = cmbLibros.getValue();

        if (libroSeleccionado == null) {
            NotificationUtil.show("Atención", "Por favor, seleccione un libro.", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        agregarAlCarrito(libroSeleccionado);

        // Limpiar selección para que quede listo para el siguiente
        cmbLibros.setValue(null);
    }

    private void agregarAlCarrito(Libro libro) {
        if (libro.getStock() <= 0) {
            NotificationUtil.show("Error", "No hay stock disponible.", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        for (ItemCarrito item : itemsCarrito) {
            if (item.getLibro().getId().equals(libro.getId())) {
                if (item.getCantidad() < libro.getStock()) {
                    item.setCantidad(item.getCantidad() + 1);
                    tablaCarrito.refresh();
                    calcularTotal();
                } else {
                    NotificationUtil.show("Info", "Stock máximo alcanzado.", true, (Stage) rootPane.getScene().getWindow());
                }
                return;
            }
        }
        itemsCarrito.add(new ItemCarrito(libro, 1));
        calcularTotal();
    }

    private void configurarTablaCarrito() {
        colCarritoTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLibro().getTitulo()));
        colCarritoCantidad.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCantidad()));
        colCarritoSubtotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSubtotal()));

        colCarritoEliminar.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("X");
            {
                btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btn.setOnAction(e -> eliminarDelCarrito(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteService.listarClientes(PageRequest.of(0, 500)).getContent();
        cmbClientes.setItems(FXCollections.observableArrayList(clientes));
        cmbClientes.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "" : c.getApellido() + " " + c.getNombre(); }
            @Override public Cliente fromString(String s) { return null; }
        });
    }

    private void cargarCuotas() {
        cmbCuotas.setItems(FXCollections.observableArrayList(1, 3, 6, 12, 18));
        cmbCuotas.setValue(1);
        cmbCuotas.setConverter(new IntegerStringConverter());
    }

    private void eliminarDelCarrito(ItemCarrito item) {
        itemsCarrito.remove(item);
        calcularTotal();
    }

    private void calcularTotal() {
        BigDecimal total = itemsCarrito.stream().map(ItemCarrito::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText("$ " + total.toString());
    }

    @FXML
    public void finalizarVenta(ActionEvent event) {
        if (cmbClientes.getValue() == null) {
            NotificationUtil.show("Error", "Seleccione un cliente.", true, (Stage) rootPane.getScene().getWindow());
            return;
        }
        if (itemsCarrito.isEmpty()) {
            NotificationUtil.show("Error", "El carrito está vacío.", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        try {
            Integer cuotas = 1;
            String textoCuotas = cmbCuotas.getEditor().getText();
            if (textoCuotas != null && !textoCuotas.isBlank()) {
                try {
                    cuotas = Integer.parseInt(textoCuotas);
                } catch (NumberFormatException e) {
                    NotificationUtil.show("Error", "Cuotas inválidas.", true, (Stage) rootPane.getScene().getWindow());
                    return;
                }
            } else if (cmbCuotas.getValue() != null) {
                cuotas = cmbCuotas.getValue();
            }

            List<CrearDetalleVentaRequest> detalles = itemsCarrito.stream()
                    .map(item -> CrearDetalleVentaRequest.builder().libroId(item.getLibro().getId()).cantidad(item.getCantidad()).build()).toList();

            CrearVentaRequest request = CrearVentaRequest.builder()
                    .clienteId(cmbClientes.getValue().getId()).detalles(detalles).cantidadCuotas(cuotas).build();

            ventaService.registrarVenta(request);
            NotificationUtil.show("Éxito", "Venta registrada.", false, (Stage) rootPane.getScene().getWindow());

            limpiarInterfazCompleta();

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.show("Error", "Falló la venta: " + e.getMessage(), true, (Stage) rootPane.getScene().getWindow());
        }
    }

    private void limpiarInterfazCompleta() {
        itemsCarrito.clear();
        lblTotal.setText("$ 0.00");
        cmbClientes.getSelectionModel().clearSelection();
        cmbClientes.setValue(null);
        cmbCuotas.setValue(1);
        cmbCuotas.getEditor().setText("1");

        // Solo limpiamos selección del combo de libros
        cmbLibros.setValue(null);

        // Recargar datos frescos
        cargarLibrosCombo();
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        limpiarInterfazCompleta();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @lombok.Data
    public static class ItemCarrito {
        private Libro libro;
        private int cantidad;
        public ItemCarrito(Libro libro, int cantidad) { this.libro = libro; this.cantidad = cantidad; }
        public BigDecimal getSubtotal() { return libro.getPrecioBase().multiply(new BigDecimal(cantidad)); }
    }
}