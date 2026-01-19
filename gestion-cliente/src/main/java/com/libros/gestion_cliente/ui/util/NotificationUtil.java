package com.libros.gestion_cliente.ui.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotificationUtil {

    public static void show(String title, String message, boolean isError, Stage ownerStage) {
        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        // Diseño del Toast
        Label label = new Label(title + ": " + message);
        label.setStyle("-fx-background-color: " + (isError ? "#c0392b" : "#27ae60") + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 15px; " +
                "-fx-background-radius: 10; " +
                "-fx-font-size: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        // Posicionar abajo a la derecha del padre
        toastStage.setX(ownerStage.getX() + ownerStage.getWidth() - 350);
        toastStage.setY(ownerStage.getY() + ownerStage.getHeight() - 100);

        toastStage.show();

        // Desaparecer automáticamente a los 3 segundos
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), evt -> {
            toastStage.close();
        }));
        timeline.play();
    }
}