package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.service.*;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import com.libros.gestion_cliente.infrastructure.report.CsvReporteService;
import com.libros.gestion_cliente.infrastructure.report.ReciboPdfService;
import com.libros.gestion_cliente.ui.util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component("reporteFxController")
@RequiredArgsConstructor
public class ReporteController {

    // Servicios necesarios
    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository; // Acceso directo para consultas custom
    private final CuotaRepository cuotaRepository; // Para buscar cuotas
    private final ReciboPdfService reciboPdfService; // Tu servicio de PDF
    private final CsvReporteService csvReporteService; // Tu servicio de CSV
    private final ApplicationContext applicationContext;
    private final VentaRepository ventaRepository;
    private final LibroRepository libroRepository;

    @FXML private BorderPane rootPane;

    // --- TAB 1: COBRANZAS ---
    @FXML private ComboBox<Cliente> cmbClientesCobro;
    @FXML private TableView<Cuota> tablaCuotas;
    @FXML private TableColumn<Cuota, String> colCuotaVenta;
    @FXML private TableColumn<Cuota, Integer> colCuotaNumero;
    @FXML private TableColumn<Cuota, String> colCuotaVencimiento;
    @FXML private TableColumn<Cuota, BigDecimal> colCuotaMonto;
    @FXML private TableColumn<Cuota, String> colCuotaEstado;

    // --- TAB 2: VISITAS ---
    @FXML private TableView<Cliente> tablaVisitas;
    @FXML private TableColumn<Cliente, String> colVisitaLocalidad;
    @FXML private TableColumn<Cliente, String> colVisitaCliente;
    @FXML private TableColumn<Cliente, String> colVisitaTelefono;
    @FXML private TableColumn<Cliente, String> colVisitaIntereses;
    @FXML private TableColumn<Cliente, String> colVisitaHistorial;

    // --- TAB 3: INTERESES ---
    @FXML private TextField txtTema;
    @FXML private TableView<Cliente> tablaInteresados;
    @FXML private TableColumn<Cliente, String> colIntNombre;
    @FXML private TableColumn<Cliente, String> colIntLocalidad;
    @FXML private TableColumn<Cliente, String> colIntIntereses;

    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        if (tabPane != null && tabPane.getTabs() != null) {
            for (Tab tab : tabPane.getTabs()) {
                // Crear un Label personalizado para cada pestaña
                Label label = new Label(tab.getText());
                label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 25 15 10 15;");

                tab.setGraphic(label);
                tab.setText(""); // Limpiar el texto original
            }
        }

        configurarTablas();
        cargarComboClientes();
        cargarDatosHojaDeRuta();
    }

    private void configurarTablas() {
        // Tab Cobranzas
        colCuotaVenta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVenta().getNroFactura()));
        colCuotaNumero.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNumeroCuota()));
        colCuotaVencimiento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaVencimiento().toString()));
        colCuotaMonto.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getMontoCuota()));
        colCuotaEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));

        // Tab Visitas (Muestra directamente propiedades del Cliente)
        colVisitaLocalidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocalidad()));
        colVisitaCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApellido() + " " + c.getValue().getNombre()));
        colVisitaTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));
        colVisitaIntereses.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInteresesPersonales()));

        // Tab Intereses
        colIntNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre() + " " + c.getValue().getApellido()));
        colIntLocalidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocalidad()));
        colIntIntereses.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInteresesPersonales()));

        // LÓGICA PARA MOSTRAR HISTORIAL DE LIBROS
        colVisitaHistorial.setCellValueFactory(cellData -> {
            Long clienteId = cellData.getValue().getId();
            // OJO: Hacer query en celda puede ser lento si son miles.
            // Para una app de escritorio local está "bien", pero idealmente se trae en el DTO.
            List<String> libros = ventaRepository.findLibrosCompradosPorCliente(clienteId);
            String resumen = String.join(", ", libros);
            return new SimpleStringProperty(resumen);
        });
    }

    private void cargarComboClientes() {
        // Carga simplificada de clientes para el combo
        List<Cliente> clientes = clienteService.listarClientes(PageRequest.of(0, 100)).getContent();
        cmbClientesCobro.setItems(FXCollections.observableArrayList(clientes));
        cmbClientesCobro.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente c) { return c == null ? "" : c.getNombre() + " " + c.getApellido(); }
            @Override
            public Cliente fromString(String s) { return null; }
        });
    }

    // --- ACCIONES TAB 1: COBRANZAS ---

    @FXML
    public void buscarCuotas(ActionEvent event) {
        Cliente cliente = cmbClientesCobro.getValue();
        if (cliente == null) return;

        // --- FORMA CORRECTA Y OPTIMIZADA ---
        // Llamamos a la base de datos pidiendo solo lo necesario y con las relaciones cargadas
        List<Cuota> cuotas = cuotaRepository.findByClienteId(cliente.getId());

        tablaCuotas.setItems(FXCollections.observableArrayList(cuotas));
    }

    @FXML
    public void generarReciboPdf(ActionEvent event) {
        Cuota cuota = tablaCuotas.getSelectionModel().getSelectedItem();
        if (cuota == null) {
            NotificationUtil.show("Error", "Seleccione una cuota", true, (Stage) rootPane.getScene().getWindow());
            return;
        }

        try {
            // El servicio ahora se encargará de recargar la cuota para evitar LazyException
            reciboPdfService.generarRecibo(cuota);
            tablaCuotas.refresh();
            NotificationUtil.show("Éxito", "Recibo generado en Escritorio/Recibos", false, (Stage) rootPane.getScene().getWindow());
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.show("Error", "Fallo al generar PDF: " + e.getMessage(), true, (Stage) rootPane.getScene().getWindow());
        }
    }

    @FXML
    public void exportarCsvVisitas(ActionEvent event) {
        try {
            csvReporteService.exportarClientes(tablaVisitas.getItems()); // LLAMADA REAL
            NotificationUtil.show("Éxito", "Excel guardado en Escritorio/Reportes", false, (Stage) rootPane.getScene().getWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void cargarClientesLibres(ActionEvent event) {
        cargarDatosHojaDeRuta(); // Reutilizamos la lógica
    }

    // Este es el método con la lógica pura (Sin argumentos)
    private void cargarDatosHojaDeRuta() {
        // Llama a la Query especial HU-02 de tu repositorio
        List<Cliente> clientesLibres = clienteRepository.findClientesLibresDeDeuda();
        tablaVisitas.setItems(FXCollections.observableArrayList(clientesLibres));
    }

    // --- ACCIONES TAB 3: MARKETING ---

    @FXML
    public void buscarPorTema(ActionEvent event) {
        String input = txtTema.getText();
        if (input == null || input.isBlank()) return;

        // 1. Limpiamos espacios en blanco al inicio y final
        String terminoBusqueda = input.trim();

        // 2. Inteligencia: ¿Es un libro?
        // Buscamos si existe un libro con ese título exacto (ignorando mayúsculas)
        Optional<Libro> libroDetectado = libroRepository.findByTituloIgnoreCase(terminoBusqueda);

        if (libroDetectado.isPresent()) {
            // ¡Es un libro! Usamos su temática para buscar clientes
            // Ej: Escribió "Clean Code" -> Buscamos clientes que les guste "Programación"
            String tematicaDelLibro = libroDetectado.get().getTematica();
            if (tematicaDelLibro != null && !tematicaDelLibro.isBlank()) {
                terminoBusqueda = tematicaDelLibro;
                NotificationUtil.show("Info", "Libro detectado. Buscando por temática: " + tematicaDelLibro, false, (Stage) rootPane.getScene().getWindow());
            }
        }

        // 3. Buscar clientes con ese interés (ya sea lo que escribió o la temática del libro)
        List<Cliente> interesados = clienteRepository.findByInteresesPersonalesContainingIgnoreCase(terminoBusqueda);
        tablaInteresados.setItems(FXCollections.observableArrayList(interesados));

        if (interesados.isEmpty()) {
            NotificationUtil.show("Info", "No se encontraron clientes interesados en: " + terminoBusqueda, true, (Stage) rootPane.getScene().getWindow());
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
}