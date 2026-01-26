package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.service.*;
import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.*;
import com.libros.gestion_cliente.infrastructure.report.CsvReporteService;
import com.libros.gestion_cliente.infrastructure.report.ReciboPdfService;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component("reporteFxController")
@RequiredArgsConstructor
public class ReporteController {

    private final ClienteService clienteService;
    private final CuotaRepository cuotaRepository;
    private final ReciboPdfService reciboPdfService;
    private final CsvReporteService csvReporteService;
    private final ApplicationContext applicationContext;
    private final VentaRepository ventaRepository;
    private final LibroRepository libroRepository;
    private final FamiliarRepository familiarRepository;
    private final PedidoEspecialRepository pedidoEspecialRepository;
    private final ClienteRepository clienteRepository;

    @FXML private BorderPane rootPane;
    @FXML private TabPane tabPane;

    // Tab Cobranzas
    @FXML private ComboBox<Cliente> cmbClientesCobro;
    @FXML private TableView<Cuota> tablaCuotas;
    @FXML private TableColumn<Cuota, String> colCuotaVenta;
    @FXML private TableColumn<Cuota, Integer> colCuotaNumero;
    @FXML private TableColumn<Cuota, String> colCuotaVencimiento;
    @FXML private TableColumn<Cuota, BigDecimal> colCuotaMonto;
    @FXML private TableColumn<Cuota, String> colCuotaEstado;
    @FXML private TableColumn<Cuota, Void> colCuotaAccion;

    // Tab Visitas
    @FXML private TableView<Cliente> tablaVisitas;
    @FXML private TableColumn<Cliente, String> colVisitaLocalidad;
    @FXML private TableColumn<Cliente, String> colVisitaCliente;
    @FXML private TableColumn<Cliente, String> colVisitaTelefono;
    @FXML private TableColumn<Cliente, String> colVisitaIntereses;
    @FXML private TableColumn<Cliente, Void> colVisitaHistorial;

    // Tab Intereses
    @FXML private TextField txtTema;
    @FXML private TableView<Cliente> tablaInteresados;
    @FXML private TableColumn<Cliente, String> colIntNombre;
    @FXML private TableColumn<Cliente, String> colIntLocalidad;
    @FXML private TableColumn<Cliente, String> colIntIntereses;

    // Tab Editorial
    @FXML private TableView<PedidoEspecial> tablaEditorial;
    @FXML private TableColumn<PedidoEspecial, String> colEdLibro;
    @FXML private TableColumn<PedidoEspecial, String> colEdCliente;
    @FXML private TableColumn<PedidoEspecial, String> colEdFecha;
    @FXML private TableColumn<PedidoEspecial, String> colEdEstado;
    @FXML private TableColumn<PedidoEspecial, Void> colEdAccion;

    @FXML private TextField txtEdadMin;
    @FXML private TextField txtEdadMax;
    @FXML private Label lblSugerencia;
    @FXML private TableView<Familiar> tablaFamilia;
    @FXML private TableColumn<Familiar, String> colFamCliente;
    @FXML private TableColumn<Familiar, String> colFamNombre;
    @FXML private TableColumn<Familiar, Integer> colFamEdad;
    @FXML private TableColumn<Familiar, String> colFamInteres;
    @FXML private TableColumn<Familiar, String> colFamSugerencia;

    @FXML
    public void initialize() {
        if (tabPane != null && tabPane.getTabs() != null) {
            for (Tab tab : tabPane.getTabs()) {
                Label label = new Label(tab.getText());
                label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 15 10 15;");
                tab.setGraphic(label);
                tab.setText("");
            }
        }

        configurarTablas();
        cargarComboClientes();
        cargarDatosHojaDeRuta();
        cargarPedidosPendientes();
    }

    private void configurarTablas() {
        // --- COBRANZAS ---
        colCuotaVenta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVenta().getNroFactura()));
        colCuotaNumero.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNumeroCuota()));
        colCuotaVencimiento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaVencimiento().toString()));
        colCuotaMonto.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getMontoCuota()));
        colCuotaEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));

        colCuotaAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnPagar = new Button("Registrar Pago");

            {
                btnPagar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                btnPagar.setOnAction(event -> {
                    Cuota cuota = getTableView().getItems().get(getIndex());
                    registrarPagoCuota(cuota);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Cuota c = getTableView().getItems().get(getIndex());
                    if (c.getEstado() == EstadoCuota.PAGADA) {
                        setGraphic(null);
                    } else {
                        setGraphic(btnPagar);
                    }
                }
            }
        });

        // --- VISITAS (CON BOTÓN VER HISTORIAL) ---
        colVisitaLocalidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocalidad()));
        colVisitaCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApellido() + " " + c.getValue().getNombre()));
        colVisitaTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));
        colVisitaIntereses.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInteresesPersonales()));

        // Botón Ver Historial
        colVisitaHistorial.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver Historial");

            {
                btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnVer.setMaxWidth(Double.MAX_VALUE);

                btnVer.setOnAction(event -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    // LLAMAMOS AL NUEVO MÉTODO
                    mostrarHistorialInteractivo(cliente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnVer);
            }
        });

        // --- INTERESES ---
        colIntNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre() + " " + c.getValue().getApellido()));
        colIntLocalidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocalidad()));
        colIntIntereses.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInteresesPersonales()));

        // --- EDITORIAL ---
        colEdLibro.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colEdCliente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCliente().getApellido() + " " + c.getValue().getCliente().getNombre()
        ));
        colEdFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaPedido().toString()));
        colEdEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));

        // --- CORRECCIÓN AQUÍ: Faltaba la lógica visual del ComboBox ---
        colEdAccion.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<EstadoPedido> cmbEstado = new ComboBox<>();

            {
                // Cargamos los valores posibles del Enum
                cmbEstado.setItems(FXCollections.observableArrayList(EstadoPedido.values()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); // <--- ESTO FALTABA
                if (empty) {
                    setGraphic(null);
                } else {
                    PedidoEspecial pedido = getTableView().getItems().get(getIndex());

                    // Desactivamos listener para setear valor inicial sin disparar evento
                    cmbEstado.setOnAction(null);
                    cmbEstado.setValue(pedido.getEstado());

                    // Activamos listener para detectar cambios del usuario
                    cmbEstado.setOnAction(event -> {
                        EstadoPedido nuevoEstado = cmbEstado.getValue();
                        if (pedido != null && pedido.getEstado() != nuevoEstado) {
                            pedido.setEstado(nuevoEstado);
                            pedidoEspecialRepository.save(pedido); // Persistencia inmediata

                            NotificationUtil.show("Actualizado", "Estado cambiado a: " + nuevoEstado, false, (Stage) rootPane.getScene().getWindow());

                            // Refrescamos tabla para actualizar la columna de texto (colEdEstado)
                            tablaEditorial.refresh();
                        }
                    });

                    setGraphic(cmbEstado);
                }
            }
        });

        // --- OPORTUNIDADES ---

        colFamCliente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCliente().getApellido() + " " + c.getValue().getCliente().getNombre()
        ));
        colFamNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));

// Usamos el método getEdad() que creamos en la entidad
        colFamEdad.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEdad()));

        colFamInteres.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIntereses() != null ? c.getValue().getIntereses() : "-"
        ));

// Sugerencia dinámica basada en la edad exacta de CADA niño
        colFamSugerencia.setCellValueFactory(c -> new SimpleStringProperty(generarSugerencia(c.getValue().getEdad())));
    }

    // --- MÉTODOS DE LÓGICA DE NEGOCIO ---

    @FXML
    public void cargarPedidosPendientes() {
        // Usamos el método optimizado (JOIN FETCH) para evitar LazyInitializationException
        List<PedidoEspecial> todos = pedidoEspecialRepository.findAllConCliente();

        // Filtramos en memoria para ocultar cancelados
        List<PedidoEspecial> activos = todos.stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .toList();

        tablaEditorial.setItems(FXCollections.observableArrayList(activos));
    }

    private void registrarPagoCuota(Cuota cuota) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Registrar Pago");
        alert.setHeaderText("Confirmar pago de Cuota " + cuota.getNumeroCuota());
        alert.setContentText("¿Desea marcar esta cuota como PAGADA?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // 1. Guardar pago
            cuota.setEstado(EstadoCuota.PAGADA);
            cuota.setFechaPagoReal(LocalDate.now());
            cuotaRepository.save(cuota);

            // 2. Verificar fin de venta
            boolean ventaFinalizada = verificarFinVenta(cuota.getVenta());

            // 3. Notificación
            if (ventaFinalizada) {
                Alert alertaFinal = new Alert(Alert.AlertType.INFORMATION);
                alertaFinal.setTitle("¡Felicidades!");
                alertaFinal.setHeaderText("Venta Finalizada Exitosamente");
                alertaFinal.setContentText("Se ha abonado la última cuota.\nEl cliente ha pasado a la lista de 'Libres de Deuda'.");
                alertaFinal.initOwner((Stage) rootPane.getScene().getWindow());
                alertaFinal.showAndWait();
            } else {
                NotificationUtil.show("Éxito", "Pago registrado correctamente.", false, (Stage) rootPane.getScene().getWindow());
            }

            // 4. Refrescar tabla
            if (cmbClientesCobro.getValue() != null) {
                buscarCuotas(null);
            }
        }
    }

    private boolean verificarFinVenta(Venta venta) {
        List<Cuota> cuotasDeLaVenta = cuotaRepository.findByVentaId(venta.getId());
        boolean todasPagadas = cuotasDeLaVenta.stream()
                .allMatch(c -> c.getEstado() == EstadoCuota.PAGADA);

        if (todasPagadas) {
            venta.setEstado(EstadoVenta.FINALIZADA);
            ventaRepository.save(venta);
            return true;
        }
        return false;
    }

    // --- EVENTOS FXML ---

    @FXML
    public void generarReciboPdf(ActionEvent event) {
        Cuota cuota = tablaCuotas.getSelectionModel().getSelectedItem();
        if (cuota == null) {
            NotificationUtil.show("Error", "Seleccione una cuota", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        if (cuota.getEstado() == EstadoCuota.PAGADA) {
            NotificationUtil.show("Info", "Esta cuota ya está PAGADA. No se puede generar recibo.", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        try {
            reciboPdfService.generarRecibo(cuota);
            NotificationUtil.show("Éxito", "Recibo generado en Escritorio", false, (Stage) rootPane.getScene().getWindow());
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.show("Error", "Fallo al generar PDF: " + e.getMessage(), true, (Stage) rootPane.getScene().getWindow());
        }
    }

    @FXML
    public void buscarPorEdadFamilia(ActionEvent event) {
        try {
            // 1. Obtenemos el rango de edad deseado (Ej: 5 a 8 años)
            int edadMin = Integer.parseInt(txtEdadMin.getText());
            int edadMax = Integer.parseInt(txtEdadMax.getText());

            if (edadMin > edadMax) {
                NotificationUtil.show("Error", "La edad mínima no puede ser mayor a la máxima", true, (Stage) rootPane.getScene().getWindow());
                return;
            }

            // 2. Calculamos los años de nacimiento correspondientes
            // Ej: Estamos en 2026.
            // Si quiero alguien de máximo 8 años -> Nació en 2018 (2026 - 8) -> Este es el año INICIO (más antiguo)
            // Si quiero alguien de mínimo 5 años -> Nació en 2021 (2026 - 5) -> Este es el año FIN (más reciente)

            int currentYear = java.time.Year.now().getValue();
            int anioNacimientoInicio = currentYear - edadMax;
            int anioNacimientoFin = currentYear - edadMin;

            // 3. Ejecutamos la búsqueda
            List<Familiar> resultados = familiarRepository.buscarPorRangoAnio(anioNacimientoInicio, anioNacimientoFin);

            // 4. Mostramos resultados
            tablaFamilia.setItems(FXCollections.observableArrayList(resultados));

            // Actualizar label de sugerencia general
            lblSugerencia.setText("Sugerencia: " + generarSugerencia(edadMin));

            if (resultados.isEmpty()) {
                NotificationUtil.show("Info", "No se encontraron familiares nacidos entre " + anioNacimientoInicio + " y " + anioNacimientoFin, true, (Stage) rootPane.getScene().getWindow());
            }

        } catch (NumberFormatException e) {
            NotificationUtil.show("Error", "Ingrese edades válidas (números enteros).", true, (Stage) rootPane.getScene().getWindow());
        }
    }

    // LÓGICA DE NEGOCIO: Qué vender según la edad
    private String generarSugerencia(int edad) {
        if (edad <= 2) return "Libros de tela, goma, texturas, sonidos.";
        if (edad <= 5) return "Cuentos cortos, aprender a leer, didácticos.";
        if (edad <= 8) return "Primeras lecturas, Dinosaurios, Animales, Disney.";
        if (edad <= 12) return "Aventuras, Comics, Gaturro, Gravity Falls.";
        if (edad <= 15) return "Novelas juveniles, Manga, Ciencia Ficción.";
        if (edad <= 18) return "Romance juvenil, Distopías, Terror.";
        return "Literatura general, Historia, Política.";
    }

    @FXML
    public void buscarCuotas(ActionEvent event) {
        Cliente cliente = cmbClientesCobro.getValue();
        if (cliente == null) return;
        List<Cuota> cuotas = cuotaRepository.findByClienteId(cliente.getId());
        tablaCuotas.setItems(FXCollections.observableArrayList(cuotas));
    }

    @FXML
    public void nuevoPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pedido_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            PedidoFormController controller = loader.getController();
            controller.setOnSaveSuccess(this::cargarPedidosPendientes);
            Stage stage = new Stage();
            stage.setTitle("Nuevo Pedido a Editorial");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void borrarPedidos(ActionEvent event) {
        // 1. Seguridad: Si la tabla está vacía, no hacemos nada
        if (tablaEditorial.getItems().isEmpty()) {
            NotificationUtil.show("Info", "La lista ya está vacía.", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        // 2. Pedir Confirmación (Popup)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Borrar Pedidos");
        alert.setHeaderText("¿Estás seguro de vaciar la tabla?");
        alert.setContentText("Se eliminarán permanentemente los " + tablaEditorial.getItems().size() + " pedidos listados.\n\nEsta acción NO se puede deshacer.");

        // 3. Ejecutar solo si el usuario da OK
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Borramos de la BD todos los elementos que están visibles en la tabla
                pedidoEspecialRepository.deleteAll(tablaEditorial.getItems());

                // Refrescamos la tabla (ahora quedará vacía)
                cargarPedidosPendientes();

                NotificationUtil.show("Éxito", "Se han eliminado los pedidos.", false, (Stage) rootPane.getScene().getWindow());
            } catch (Exception e) {
                e.printStackTrace();
                NotificationUtil.show("Error", "No se pudo borrar: " + e.getMessage(), true, (Stage) rootPane.getScene().getWindow());
            }
        }
    }

    @FXML
    public void generarPdfEditorial(ActionEvent event) {
        // 1. Obtener datos frescos y filtrar SOLO los PENDIENTES
        // Usamos findAllConCliente() para asegurar que no falle por LazyLoading
        List<PedidoEspecial> soloPendientes = pedidoEspecialRepository.findAllConCliente().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE)
                .toList();

        // 2. Validar si hay algo para imprimir
        if (soloPendientes.isEmpty()) {
            NotificationUtil.show("Info", "No hay pedidos PENDIENTES para solicitar a la editorial.", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        try {
            // 3. Generar PDF con la lista filtrada
            reciboPdfService.generarListaReposicion(soloPendientes);
            NotificationUtil.show("Éxito", "Lista de Compra generada en Escritorio", false, (Stage) rootPane.getScene().getWindow());
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.show("Error", "Fallo al generar PDF: " + e.getMessage(), true, (Stage) rootPane.getScene().getWindow());
        }
    }

    @FXML public void exportarCsvVisitas(ActionEvent event) {
        try {
            csvReporteService.exportarClientes(tablaVisitas.getItems());
            NotificationUtil.show("Éxito", "CSV generado", false, (Stage) rootPane.getScene().getWindow());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void cargarClientesLibres(ActionEvent event) {
        cargarDatosHojaDeRuta();
    }

    private void cargarDatosHojaDeRuta() {
        List<Cliente> clientes = clienteRepository.findClientesLibresDeDeuda();
        tablaVisitas.setItems(FXCollections.observableArrayList(clientes));
    }

    private void cargarComboClientes() {
        List<Cliente> clientes = clienteService.listarClientes(PageRequest.of(0, 100)).getContent();
        cmbClientesCobro.setItems(FXCollections.observableArrayList(clientes));
        cmbClientesCobro.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "" : c.getNombre() + " " + c.getApellido(); }
            @Override public Cliente fromString(String s) { return null; }
        });
    }

    @FXML
    public void buscarPorTema(ActionEvent event) {
        String input = txtTema.getText();
        if (input == null || input.isBlank()) return;

        String terminoBusqueda = input.trim();
        Set<Cliente> clientesResultantes = new HashSet<>();
        Set<String> temasDetectados = new HashSet<>();
        List<String> titulosLibros = new ArrayList<>();

        // PASO 1: Buscar TODOS los libros que coincidan
        List<Libro> librosEncontrados = libroRepository.findByTituloContainingIgnoreCase(terminoBusqueda);

        if (!librosEncontrados.isEmpty()) {
            for (Libro libro : librosEncontrados) {
                if (libro.getTematica() != null && !libro.getTematica().isBlank()) {
                    temasDetectados.add(libro.getTematica());
                    titulosLibros.add(libro.getTitulo());
                    List<Cliente> porTema = clienteRepository.findByInteresesPersonalesContainingIgnoreCase(libro.getTematica());
                    clientesResultantes.addAll(porTema);
                }
            }
        }

        // PASO 2: Buscar también literal
        List<Cliente> porInputDirecto = clienteRepository.findByInteresesPersonalesContainingIgnoreCase(terminoBusqueda);
        clientesResultantes.addAll(porInputDirecto);

        tablaInteresados.setItems(FXCollections.observableArrayList(clientesResultantes));

        if (!temasDetectados.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Libros encontrados (").append(librosEncontrados.size()).append("):\n");
            titulosLibros.stream().limit(3).forEach(t -> msg.append("- ").append(t).append("\n"));
            if (titulosLibros.size() > 3) msg.append("... y otros.\n");
            msg.append("\nSe agregaron clientes interesados en: ").append(temasDetectados);
            NotificationUtil.show("Búsqueda Expandida", msg.toString(), false, (Stage) rootPane.getScene().getWindow());
        } else if (clientesResultantes.isEmpty()) {
            NotificationUtil.show("Sin resultados", "No se encontraron libros ni clientes con: " + input, true, (Stage) rootPane.getScene().getWindow());
        }
    }

    private void mostrarHistorialPopup(Cliente cliente) {
        // 1. Buscamos los libros SOLO cuando el usuario lo pide (Mejora rendimiento)
        List<String> libros = ventaRepository.findLibrosCompradosPorCliente(cliente.getId());

        // 2. Preparamos el mensaje
        String contenido;
        if (libros.isEmpty()) {
            contenido = "Este cliente no registra compras históricas.";
        } else {
            // Ponemos cada libro en una línea nueva con un guion
            contenido = "- " + String.join("\n- ", libros);
        }

        // 3. Creamos el Popup (Alert)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historial de Compras");
        alert.setHeaderText("Cliente: " + cliente.getApellido() + " " + cliente.getNombre());

        // Usamos un TextArea dentro del Alert por si la lista es MUY larga
        TextArea textArea = new TextArea(contenido);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        // Ajuste visual para que el TextArea se vea bien dentro del Alert
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        // Reemplazamos el contenido por defecto con nuestro TextArea scrolleable
        alert.getDialogPane().setContent(expContent);
        alert.initOwner(rootPane.getScene().getWindow()); // Centrar en la ventana

        alert.showAndWait();
    }

    private void mostrarHistorialInteractivo(Cliente cliente) {
        // 1. Obtener datos
        List<String> libros = ventaRepository.findLibrosCompradosPorCliente(cliente.getId());

        // 2. Crear ventana (Stage)
        Stage stage = new Stage();
        stage.setTitle("Historial - " + cliente.getApellido());
        stage.initOwner(rootPane.getScene().getWindow());

        // 3. Componentes UI
        TextField txtBuscarLibro = new TextField();
        txtBuscarLibro.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        ListView<String> listaView = new ListView<>();
        listaView.setStyle("-fx-font-size: 13px;");
        VBox.setVgrow(listaView, Priority.ALWAYS);

        // --- LÓGICA DE VACÍO VS LLENO ---
        if (libros.isEmpty()) {
            // CASO A: No hay libros
            txtBuscarLibro.setDisable(true); // Bloqueamos el buscador
            txtBuscarLibro.setPromptText("⛔ Sin historial disponible");

            // Mostramos mensaje en la lista
            listaView.getItems().add("Este cliente no ha comprado libros aún.");
            listaView.setDisable(true); // Opcional: para que se vea "apagada"
        } else {
            // CASO B: Sí hay libros (Lógica normal con filtro)
            txtBuscarLibro.setDisable(false);
            txtBuscarLibro.setPromptText("🔍 Buscar libro (" + libros.size() + " encontrados)...");

            ObservableList<String> datosMaster = FXCollections.observableArrayList(libros);
            FilteredList<String> datosFiltrados = new FilteredList<>(datosMaster, p -> true);

            listaView.setItems(datosFiltrados);

            // Listener de búsqueda
            txtBuscarLibro.textProperty().addListener((obs, oldVal, newVal) -> {
                datosFiltrados.setPredicate(libro -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    return libro.toLowerCase().contains(newVal.toLowerCase());
                });
            });
        }

        // 4. Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #333333;");

        Label titulo = new Label("Historial: " + cliente.getNombre() + " " + cliente.getApellido());
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        layout.getChildren().addAll(titulo, txtBuscarLibro, listaView);

        // 5. Cerrar con ESC
        layout.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) stage.close();
        });

        Scene scene = new Scene(layout, 500, 600);
        stage.setScene(scene);
        stage.show();

        // Si hay libros, ponemos el foco para escribir. Si no, foco en el botón cerrar (X)
        if (!libros.isEmpty()) {
            txtBuscarLibro.requestFocus();
        }
    }

    @FXML public void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}