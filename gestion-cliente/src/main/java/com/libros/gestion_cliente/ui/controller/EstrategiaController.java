package com.libros.gestion_cliente.ui.controller;

import com.libros.gestion_cliente.application.dto.ReporteItem;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
public class EstrategiaController {

    private final VentaRepository ventaRepository;
    private final ApplicationContext applicationContext;

    @FXML private BorderPane rootPane;
    @FXML private PieChart pieLibros;
    @FXML private BarChart<String, Number> barClientes;

    @FXML
    public void initialize() {
        rootPane.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(800), rootPane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        cargarDatos();
    }

    @FXML
    public void cargarDatos() {
        // 1. Cargar Gráfico de Libros (PieChart)
        List<ReporteItem> topLibros = ventaRepository.obtenerLibrosMasVendidos();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (ReporteItem item : topLibros) {
            // Formateamos la etiqueta para que muestre Nombre y Cantidad
            String etiqueta = String.format("%s (%d)", item.getEtiqueta(), item.getValor().intValue());
            pieData.add(new PieChart.Data(etiqueta, item.getValor().doubleValue()));
        }
        pieLibros.setData(pieData);
        // (Se eliminó el bloque de código de los Tooltips aquí)

        // 2. Cargar Gráfico de Clientes (BarChart)
        List<ReporteItem> topClientes = ventaRepository.obtenerMejoresClientes();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Compras");

        for (ReporteItem item : topClientes) {
            series.getData().add(new XYChart.Data<>(item.getEtiqueta(), item.getValor()));
        }

        barClientes.getData().clear();
        barClientes.getData().add(series);
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