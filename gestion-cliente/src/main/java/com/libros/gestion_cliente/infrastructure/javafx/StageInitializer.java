package com.libros.gestion_cliente.infrastructure.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Value("classpath:/fxml/main.fxml") // El archivo visual principal
    private Resource mainFxml;

    private final ApplicationContext applicationContext;
    private final String applicationTitle = "Sistema de Gestión - FernaLibros";

    public StageInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(mainFxml.getURL());
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            Stage stage = event.getStage();

            stage.setScene(new Scene(parent, 1024, 768));
            stage.setTitle(applicationTitle);
            stage.setResizable(true); // <--- ASEGURAR QUE SE PUEDA REDIMENSIONAR
            stage.setMaximized(true); // Arranca con pantalla completa
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la interfaz gráfica", e);
        }
    }
}