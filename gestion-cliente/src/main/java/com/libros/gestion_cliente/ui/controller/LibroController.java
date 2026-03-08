package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component("libroFxController")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;
    private final LibroRepository libroRepository;
    private final ApplicationContext applicationContext;

    @FXML private BorderPane rootPane;
    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colTematica;
    @FXML private TableColumn<Libro, Double> colPrecio;
    @FXML private TableColumn<Libro, Integer> colStock;
    @FXML private TableColumn<Libro, Void> colAccionStock;
    @FXML private TextField txtBuscar;
    @FXML private Label lblPagina;

    private int paginaActual = 0;
    private int totalPaginas = 0;
    private final int TAMANO_PAGINA = 10;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarLibros();

        // Búsqueda en tiempo real o al limpiar
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) cargarLibros();
        });
    }

    private void configurarTabla() {
        // --- 1. HACER LA TABLA EDITABLE ---
        tablaLibros.setEditable(true);

        // --- 2. CONFIGURAR COLUMNAS NORMALES ---
        colIsbn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIsbn()));
        colAutor.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAutor()));
        colTematica.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTematica() != null ? cell.getValue().getTematica() : "-"));
        colStock.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStock()));

        // --- 3. CONFIGURAR COLUMNA TÍTULO (EDITABLE CON DOBLE CLIC) ---
        colTitulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitulo()));
        colTitulo.setCellFactory(TextFieldTableCell.forTableColumn());
        colTitulo.setOnEditCommit(event -> {
            Libro libroEditado = event.getRowValue();
            String nuevoTitulo = event.getNewValue();

            if (nuevoTitulo != null && !nuevoTitulo.trim().isEmpty()) {
                libroEditado.setTitulo(nuevoTitulo.trim());
                libroRepository.save(libroEditado);
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Error", "El título no puede estar vacío.");
                tablaLibros.refresh(); // Vuelve a poner el título viejo si intentó dejarlo en blanco
            }
        });

        // --- 4. CONFIGURAR COLUMNA PRECIO (EDITABLE CON DOBLE CLIC) ---
        colPrecio.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrecioBase() != null ? cell.getValue().getPrecioBase().doubleValue() : 0.0));
        colPrecio.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colPrecio.setOnEditCommit(event -> {
            Libro libroEditado = event.getRowValue();
            Double nuevoPrecio = event.getNewValue();

            if (nuevoPrecio != null && nuevoPrecio >= 0) {
                libroEditado.setPrecioBase(BigDecimal.valueOf(nuevoPrecio));
                libroRepository.save(libroEditado);
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Error", "El precio no puede ser negativo o nulo.");
                tablaLibros.refresh(); // Vuelve a poner el precio viejo visualmente
            }
        });

        // --- 5. CONFIGURAR BOTONES DE STOCK (+ y -) ---
        colAccionStock.setCellFactory(param -> new TableCell<>() {
            private final Button btnSumar = new Button("+");
            private final Button btnRestar = new Button("-");
            private final HBox container = new HBox(5);

            {
                btnRestar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 30px;");
                btnRestar.setOnAction(event -> {
                    Libro libro = getTableView().getItems().get(getIndex());
                    restarStock(libro);
                });

                btnSumar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 30px;");
                btnSumar.setOnAction(event -> {
                    Libro libro = getTableView().getItems().get(getIndex());
                    sumarStock(libro);
                });

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(btnRestar, btnSumar);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    // --- MÉTODOS DE BÚSQUEDA Y PAGINACIÓN ---
    @FXML
    public void buscarLibro(ActionEvent event) {
        String termino = txtBuscar.getText();
        if (termino == null || termino.isBlank()) {
            cargarLibros();
            return;
        }

        List<Libro> resultados = libroRepository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(termino.trim(), termino.trim());
        tablaLibros.setItems(FXCollections.observableArrayList(resultados));
        lblPagina.setText("Resultados: " + resultados.size());
    }

    @FXML
    public void limpiarBusqueda(ActionEvent event) {
        txtBuscar.clear();
        cargarLibros();
    }

    private void cargarLibros() {
        Page<Libro> pagina = libroService.listarLibros(PageRequest.of(paginaActual, TAMANO_PAGINA));
        this.totalPaginas = pagina.getTotalPages();
        tablaLibros.getItems().setAll(pagina.getContent());
        int displayTotal = totalPaginas > 0 ? totalPaginas : 1;
        lblPagina.setText("Página " + (paginaActual + 1) + " de " + displayTotal);
    }

    @FXML
    public void anterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarLibros();
        }
    }

    @FXML
    public void siguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            cargarLibros();
        }
    }

    // --- LÓGICA DE STOCK ---
    private void sumarStock(Libro libro) {
        libro.setStock(libro.getStock() + 1);
        libroRepository.save(libro);
        tablaLibros.refresh();
    }

    private void restarStock(Libro libro) {
        if (libro.getStock() > 0) {
            libro.setStock(libro.getStock() - 1);
            libroRepository.save(libro);
            tablaLibros.refresh();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Límite alcanzado", "El stock no puede ser negativo.");
        }
    }

    // --- NAVEGACIÓN Y ALERTAS ---
    @FXML
    public void volverAlMenu(ActionEvent event) {
        navegar("/fxml/main.fxml");
    }

    @FXML
    public void irANuevoLibro(ActionEvent event) {
        navegar("/fxml/libro_form.fxml"); // Asumo que aquí tienes tu formulario de creación aislado
    }

    private void navegar(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) tablaLibros.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la pantalla: " + fxmlPath);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}