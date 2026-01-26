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
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import java.util.List;

import java.io.IOException;

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

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) cargarLibros();
        });
    }

    private void configurarTabla() {
        colIsbn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIsbn()));
        colTitulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitulo()));
        colAutor.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAutor()));
        colTematica.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTematica() != null ? cell.getValue().getTematica() : "-"));
        colPrecio.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrecioBase().doubleValue()));
        colStock.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStock()));

        colAccionStock.setCellFactory(param -> new TableCell<>() {
            private final Button btnSumar = new Button("+1");
            {
                btnSumar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btnSumar.setOnAction(event -> {
                    Libro libro = getTableView().getItems().get(getIndex());
                    sumarStock(libro);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnSumar);
            }
        });
    }

    @FXML
    public void buscarLibro(ActionEvent event) {
        String termino = txtBuscar.getText();
        if (termino == null || termino.isBlank()) {
            cargarLibros();
            return;
        }

        // Búsqueda inteligente: Título O Autor
        List<Libro> resultados = libroRepository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(termino.trim(), termino.trim());

        tablaLibros.setItems(FXCollections.observableArrayList(resultados));
        lblPagina.setText("Resultados: " + resultados.size());
    }

    @FXML
    public void limpiarBusqueda(ActionEvent event) {
        txtBuscar.clear();
        cargarLibros(); // Vuelve a la paginación normal
    }

    private void sumarStock(Libro libro) {
        libro.setStock(libro.getStock() + 1);
        libroRepository.save(libro);
        tablaLibros.refresh();
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

    @FXML
    public void volverAlMenu(ActionEvent event) {
        navegar("/fxml/main.fxml");
    }

    @FXML
    public void irANuevoLibro(ActionEvent event) {
        navegar("/fxml/libro_form.fxml");
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
        }
    }
}