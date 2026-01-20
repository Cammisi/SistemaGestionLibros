package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.service.VentaService;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.ui.model.VentaFx;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HistorialVentasController {

    private final VentaService ventaService;
    private final ApplicationContext applicationContext;

    @FXML private BorderPane rootPane;
    @FXML private TableView<VentaFx> tablaVentas;
    @FXML private TableColumn<VentaFx, String> colFactura;
    @FXML private TableColumn<VentaFx, String> colFecha;
    @FXML private TableColumn<VentaFx, String> colCliente;
    @FXML private TableColumn<VentaFx, String> colEstado;
    @FXML private TableColumn<VentaFx, String> colTotal;

    @FXML
    public void initialize() {
        // Animación
        rootPane.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(500), rootPane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // Configurar columnas
        colFactura.setCellValueFactory(cell -> cell.getValue().getNroFactura());
        colFecha.setCellValueFactory(cell -> cell.getValue().getFecha());
        colCliente.setCellValueFactory(cell -> cell.getValue().getClienteNombre());
        colEstado.setCellValueFactory(cell -> cell.getValue().getEstado());
        colTotal.setCellValueFactory(cell -> cell.getValue().getMontoTotal());

        cargarVentas();
    }

    @FXML
    public void cargarVentas() {
        List<Venta> ventas = ventaService.listarVentasRecientes();
        List<VentaFx> ventasFx = ventas.stream().map(VentaFx::new).toList();
        tablaVentas.setItems(FXCollections.observableArrayList(ventasFx));
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
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
}