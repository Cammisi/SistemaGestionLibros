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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("ventaFxController") // Nombre único para evitar conflictos
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final LibroService libroService;
    private final ClienteService clienteService;
    private final ApplicationContext applicationContext;

    @FXML private BorderPane rootPane;
    @FXML private ComboBox<Cliente> cmbClientes;
    @FXML private TextField txtBuscarLibro;
    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> colLibroTitulo;
    @FXML private TableColumn<Libro, String> colLibroAutor;
    @FXML private TableColumn<Libro, BigDecimal> colLibroPrecio;
    @FXML private TableColumn<Libro, Integer> colLibroStock;
    @FXML private TableColumn<Libro, Void> colLibroAccion;

    @FXML private TableView<ItemCarrito> tablaCarrito;
    @FXML private TableColumn<ItemCarrito, String> colCarritoTitulo;
    @FXML private TableColumn<ItemCarrito, Integer> colCarritoCantidad;
    @FXML private TableColumn<ItemCarrito, BigDecimal> colCarritoSubtotal;
    @FXML private TableColumn<ItemCarrito, Void> colCarritoEliminar;

    @FXML private ComboBox<Integer> cmbCuotas; // <--- NUEVO SELECTOR
    @FXML private Label lblTotal;

    private final ObservableList<ItemCarrito> itemsCarrito = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTablas();
        cargarClientes();
        cargarCuotas(); // <--- Inicializar combo de cuotas
        tablaCarrito.setItems(itemsCarrito);
    }

    private void cargarCuotas() {
        // Opciones estándar de cuotas
        cmbCuotas.setItems(FXCollections.observableArrayList(1, 3, 6, 12));
        cmbCuotas.setValue(1); // Por defecto 1 cuota (Contado)
    }

    private void configurarTablas() {
        // --- TABLA LIBROS ---
        colLibroTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colLibroAutor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAutor()));
        colLibroPrecio.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrecioBase()));
        colLibroStock.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStock()));

        colLibroAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Agregar");
            {
                btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(e -> agregarAlCarrito(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // --- TABLA CARRITO ---
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
        cmbClientes.setItems(FXCollections.observableArrayList(clienteService.listarClientes(PageRequest.of(0, 100)).getContent()));
        cmbClientes.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "" : c.getApellido() + " " + c.getNombre(); }
            @Override public Cliente fromString(String s) { return null; }
        });
    }

    @FXML
    public void buscarLibro(ActionEvent event) {
        String termino = txtBuscarLibro.getText();
        if (termino != null && !termino.isBlank()) {
            List<Libro> libros = libroService.listarLibros(PageRequest.of(0, 20)).getContent().stream()
                    .filter(l -> l.getTitulo().toLowerCase().contains(termino.toLowerCase()) || l.getIsbn().contains(termino))
                    .collect(Collectors.toList());
            tablaLibros.setItems(FXCollections.observableArrayList(libros));
        }
    }

    private void agregarAlCarrito(Libro libro) {
        if (libro.getStock() <= 0) {
            NotificationUtil.show("Error", "No hay stock disponible", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        // Verificar si ya está en carrito
        for (ItemCarrito item : itemsCarrito) {
            if (item.getLibro().getId().equals(libro.getId())) {
                if (item.getCantidad() < libro.getStock()) {
                    item.setCantidad(item.getCantidad() + 1);
                    tablaCarrito.refresh();
                    calcularTotal();
                } else {
                    NotificationUtil.show("Info", "Máximo stock alcanzado", true, (Stage) rootPane.getScene().getWindow());
                }
                return;
            }
        }

        // Si no está, agregar nuevo
        itemsCarrito.add(new ItemCarrito(libro, 1));
        calcularTotal();
    }

    private void eliminarDelCarrito(ItemCarrito item) {
        itemsCarrito.remove(item);
        calcularTotal();
    }

    private void calcularTotal() {
        BigDecimal total = itemsCarrito.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText("$ " + total.toString());
    }

    @FXML
    public void finalizarVenta(ActionEvent event) {
        if (cmbClientes.getValue() == null) {
            NotificationUtil.show("Error", "Seleccione un cliente", true, (Stage) rootPane.getScene().getWindow());
            return;
        }
        if (itemsCarrito.isEmpty()) {
            NotificationUtil.show("Error", "El carrito está vacío", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        try {
            // Convertir Items Carrito a DTO
            List<CrearDetalleVentaRequest> detalles = itemsCarrito.stream()
                    .map(item -> CrearDetalleVentaRequest.builder()
                            .libroId(item.getLibro().getId())
                            .cantidad(item.getCantidad())
                            .build())
                    .toList();

            CrearVentaRequest request = CrearVentaRequest.builder()
                    .clienteId(cmbClientes.getValue().getId())
                    .detalles(detalles)
                    .cantidadCuotas(cmbCuotas.getValue()) // <--- USAR EL VALOR DEL COMBOBOX
                    .build();

            ventaService.registrarVenta(request);

            NotificationUtil.show("Éxito", "Venta registrada correctamente", false, (Stage) rootPane.getScene().getWindow());

            // Limpiar pantalla
            itemsCarrito.clear();
            lblTotal.setText("$ 0.00");
            cmbClientes.getSelectionModel().clearSelection();
            cmbCuotas.setValue(1); // Resetear a 1

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.show("Error", "Falló la venta: " + e.getMessage(), true, (Stage) rootPane.getScene().getWindow());
        }
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clase auxiliar simple para la tabla
    @lombok.Data
    public static class ItemCarrito {
        private Libro libro;
        private int cantidad;

        public ItemCarrito(Libro libro, int cantidad) {
            this.libro = libro;
            this.cantidad = cantidad;
        }

        public BigDecimal getSubtotal() {
            return libro.getPrecioBase().multiply(new BigDecimal(cantidad));
        }
    }
}