# Guía de Contribución

¡Gracias por tu interés en contribuir a GestionLibros!

## Código de Conducta
Este proyecto se adhiere a estándares profesionales de comunicación. Se espera respeto mutuo en Issues y Pull Requests.

## ¿Cómo contribuir?

### 1. Reportar Bugs
* Asegúrate de que el bug no haya sido reportado previamente en [Issues].
* Abre un issue con el título: `bug: descripción corta`.
* Incluye pasos para reproducir el error y el comportamiento esperado.

### 2. Sugerir Funcionalidades
* Abre un issue con el título: `feat: descripción de la idea`.
* Explica por qué esta funcionalidad sería útil para el usuario final (el vendedor).

### 3. Pull Requests (PR)
1. Haz un **Fork** del repositorio.
2. Crea una rama para tu feature: `git checkout -b feature/nombre-tarea`.
3. Asegúrate de que tu código sigue el estilo del proyecto (Java 21, Checkstyle).
4. **Agrega Tests:** No se aceptan PRs que bajen la cobertura de código (< 80%).
5. Ejecuta los tests localmente: `./mvnw verify`.
6. Haz commit usando **Conventional Commits** (ej: `feat: ...`, `fix: ...`).
7. Abre el PR apuntando a la rama `develop`.

## Estándares de Código
* Usamos **Spring Boot 3.5** y **Java 21**.
* La arquitectura es **Hexagonal**. Respeta las capas (`domain`, `application`, `infrastructure`).
* Evita lógica de negocio en los controladores de UI.

¡Happy Coding!