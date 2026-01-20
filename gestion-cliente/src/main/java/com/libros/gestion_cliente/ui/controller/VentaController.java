package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.CrearDetalleVentaRequest;
import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.application.service.VentaService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.ui.model.ItemCarritoFx;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component("ventaFxController")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final LibroService libroService;
    private final ClienteService clienteService;
    private final ApplicationContext applicationContext;

    @FXML private BorderPane rootPane;
    @FXML private ComboBox<Cliente> cmbClientes;
    @FXML private ComboBox<Libro> cmbLibros;
    @FXML private Spinner<Integer> spinnerCantidad;

    @FXML private TableView<ItemCarritoFx> tablaCarrito;
    @FXML private TableColumn<ItemCarritoFx, String> colProducto;
    @FXML private TableColumn<ItemCarritoFx, BigDecimal> colPrecio;
    @FXML private TableColumn<ItemCarritoFx, Integer> colCantidad;
    @FXML private TableColumn<ItemCarritoFx, BigDecimal> colSubtotal;
    @FXML private TableColumn<ItemCarritoFx, Void> colAccion;

    @FXML private Label lblTotal;

    private final ObservableList<ItemCarritoFx> carrito = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarAnimacion();
        configurarTablas();
        cargarDatosIniciales();
        configurarSpinner();
    }

    private void configurarAnimacion() {
        rootPane.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(500), rootPane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void configurarTablas() {
        // Enlazar columnas de datos
        colProducto.setCellValueFactory(cell -> cell.getValue().getTitulo());
        colPrecio.setCellValueFactory(cell -> cell.getValue().getPrecioUnitario());
        colCantidad.setCellValueFactory(cell -> cell.getValue().getCantidad().asObject());
        colSubtotal.setCellValueFactory(cell -> cell.getValue().getSubtotal());

        // --- CONFIGURACIÓN DEL BOTÓN ELIMINAR ---
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button("X");

            {
                // Estilo rojo para el botón
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btnEliminar.setOnAction(event -> {
                    // Obtener el item de la fila actual
                    ItemCarritoFx item = getTableView().getItems().get(getIndex());
                    // Eliminar del carrito
                    carrito.remove(item);
                    // Recalcular el total general
                    actualizarTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEliminar);
                }
            }
        });

        tablaCarrito.setItems(carrito);
    }

    private void limpiarFormulario() {
        carrito.clear(); // Vacía la lista observable y la tabla
        lblTotal.setText("$ 0.00");

        if (cmbClientes != null) cmbClientes.getSelectionModel().clearSelection();
        if (cmbLibros != null) cmbLibros.getSelectionModel().clearSelection();
        if (spinnerCantidad != null) spinnerCantidad.getValueFactory().setValue(1);
    }

    private void configurarSpinner() {
        // Spinner de 1 a 100, valor inicial 1
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinnerCantidad.setValueFactory(valueFactory);
    }

    private void cargarDatosIniciales() {
        // 1. Cargar Clientes (Traemos los primeros 50 para el combo, idealmente sería búsqueda dinámica)
        List<Cliente> clientes = clienteService.listarClientes(PageRequest.of(0, 50)).getContent();
        cmbClientes.setItems(FXCollections.observableArrayList(clientes));

        // Convertidor para mostrar "Nombre Apellido" en el ComboBox en vez del toString feo
        cmbClientes.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getNombre() + " " + c.getApellido() + " (DNI: " + c.getDni() + ")";
            }
            @Override
            public Cliente fromString(String string) {
                return null; // No necesario para selección
            }
        });

        // 2. Cargar Libros (Stock > 0)
        // OJO: Aquí deberíamos usar un método en LibroService que traiga solo disponibles.
        // Por ahora listamos todos paginados.
        List<Libro> libros = libroService.listarLibros(PageRequest.of(0, 100)).getContent();
        cmbLibros.setItems(FXCollections.observableArrayList(libros));

        cmbLibros.setConverter(new StringConverter<>() {
            @Override
            public String toString(Libro l) {
                return l == null ? "" : l.getTitulo() + " - $" + l.getPrecioBase();
            }
            @Override
            public Libro fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    public void agregarAlCarrito(ActionEvent event) {
        Libro libroSeleccionado = cmbLibros.getValue();
        Integer cantidad = spinnerCantidad.getValue();

        if (libroSeleccionado == null) {
            mostrarNotificacion("Atención", "Seleccione un libro primero", true);
            return;
        }

        if (libroSeleccionado.getStock() < cantidad) {
            mostrarNotificacion("Stock Insuficiente", "Solo quedan " + libroSeleccionado.getStock() + " unidades", true);
            return;
        }

        // Agregar al carrito
        carrito.add(new ItemCarritoFx(libroSeleccionado, cantidad));
        actualizarTotal();

        // Resetear selección
        cmbLibros.getSelectionModel().clearSelection();
        spinnerCantidad.getValueFactory().setValue(1);
    }

    private void actualizarTotal() {
        BigDecimal total = carrito.stream()
                .map(item -> item.getSubtotal().get())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotal.setText("$ " + total.toString());
    }

    @FXML
    public void confirmarVenta(ActionEvent event) {
        if (carrito.isEmpty()) {
            mostrarNotificacion("Error", "El carrito está vacío", true);
            return;
        }

        Cliente cliente = cmbClientes.getValue();
        if (cliente == null) {
            mostrarNotificacion("Error", "Debe seleccionar un cliente", true);
            return;
        }

        try {
            // 1. Convertir carrito visual a DTO de solicitud
            List<CrearDetalleVentaRequest> detalles = carrito.stream()
                    .map(item -> CrearDetalleVentaRequest.builder()
                            .libroId(item.getLibroId().get())
                            .cantidad(item.getCantidad().get())
                            .build())
                    .collect(Collectors.toList());

            // 2. Crear Request de Venta
            CrearVentaRequest request = CrearVentaRequest.builder()
                    .clienteId(cliente.getId())
                    .cantidadCuotas(1) // Por defecto contado 1 cuota
                    .detalles(detalles)
                    .build();

            // 3. Llamar al servicio
            Venta venta = ventaService.registrarVenta(request);

            mostrarNotificacion("¡Venta Exitosa!", "Factura Nro: " + venta.getNroFactura(), false);

            // Limpiar todo
            carrito.clear();
            actualizarTotal();
            cmbClientes.getSelectionModel().clearSelection();

            // Opcional: Navegar a historial o generar PDF aquí mismo

        } catch (Exception e) {
            e.printStackTrace();
            mostrarNotificacion("Error en Venta", e.getMessage(), true);
        }
    }

    @FXML
    public void nuevoCliente(ActionEvent event) {
        // Reutilizamos el formulario de clientes
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Recargar combo al cerrar el formulario
            ClienteFormController controller = loader.getController();
            controller.setOnSaveSuccess(this::cargarDatosIniciales);

            Stage modalStage = new Stage();
            modalStage.setScene(new Scene(root));
            modalStage.initOwner(rootPane.getScene().getWindow());
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        // Limpiamos antes de irnos para que al volver esté vacío
        limpiarFormulario();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Fernando Libros - Sistema de Gestión");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarNotificacion(String titulo, String mensaje, boolean isError) {
        NotificationUtil.show(titulo, mensaje, isError, (Stage) rootPane.getScene().getWindow());
    }
}