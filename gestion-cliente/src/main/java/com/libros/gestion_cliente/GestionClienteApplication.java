package com.libros.gestion_cliente;

import com.libros.gestion_cliente.infrastructure.javafx.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestionClienteApplication {

	public static void main(String[] args) {

		Application.launch(JavaFxApplication.class, args);
	}

}
