# Sistema de Gestión de Libros y Clientes

![Build Status](https://github.com/Cammisi/SistemaGestionLibros/actions/workflows/maven-build.yml/badge.svg)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

Sistema de escritorio moderno desarrollado para la gestión logística, financiera y comercial de venta de libros a domicilio. 
Diseñado con enfoque **Offline-First** y arquitectura modular.

---

## 🚀 Visión del Proyecto

El sistema reemplaza la gestión manual en papel por una solución digital robusta que permite:

1. **Gestión de Ciclo de Vida del Cliente:** Control de cuentas corrientes, historial de pagos y límites de crédito.
2. **Inteligencia de Venta:** Sugerencias automáticas de libros basadas en la composición familiar (edades de hijos) y compras previas.
3. **Logística:** Planificación de rutas de cobro y entrega por localidad.

---

## 🏗️ Arquitectura y Diseño

El proyecto sigue una **Arquitectura Hexagonal (Ports & Adapters)** para desacoplar el núcleo de negocio de la tecnología.

### Estructura de Paquetes
```text
com.libros.gestion_cliente
├── domain/             # (Core) Entidades, Reglas de Negocio y Puertos
│   ├── model/          # Entidades JPA (Cliente, Venta, Libro)
│   ├── repository/     # Interfaces (Puertos de Salida)
│   └── service/        # Interfaces (Puertos de Entrada)
│
├── application/        # (Orquestación) Implementación de casos de uso
│
├── infrastructure/     # (Adaptadores)
│   ├── persistence/    # Implementación JPA (Spring Data)
│   └── report/         # Generación de recibos PDF (JasperReports)
│
└── ui/                 # (Presentación)
    ├── controller/     # Controladores JavaFX
    └── assets/         # Estilos AtlantaFX y FXML
```

### Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| **Backend** | Java 21 LTS + Spring Boot 3.5 |
| **Frontend** | JavaFX 21 + AtlantaFX |
| **Persistencia** | PostgreSQL 16 + Spring Data JPA |
| **Reportes** | JasperReports |
| **Testing** | JUnit 5, Mockito, Testcontainers |
| **CI/CD** | GitHub Actions + Docker Compose |

**Decisiones de Diseño:**
- **ENUM nativos de PostgreSQL** para estados (`EN_PROCESO`, `PAGADA`, `CANCELADA`) garantizando integridad a nivel de BD.
- **Cálculo dinámico de edades** basado en `anio_nacimiento` para evitar datos obsoletos.
- **Schema `init.sql`** ejecutado por Docker (migración futura a Flyway planificada).

---

## 🛠️ Requisitos Previos

| Herramienta | Versión Mínima | Obligatorio |
|-------------|----------------|-------------|
| Docker Desktop | 24.0+ | ✅ Sí |
| Java JDK | 21 | ✅ Sí |
| Maven | 3.9+ | ❌ No (incluye wrapper) |

---

## ⚡ Quick Start

### 1️⃣ Levantar Infraestructura

Desde la raíz del proyecto (donde está este README):
```bash
docker-compose up -d
```

Esto iniciará:
- **PostgreSQL 16:** Puerto `5433` (mapeado desde `5432` interno para evitar colisiones).
- **pgAdmin 4:** [http://localhost:5050](http://localhost:5050)
  - Usuario: `admin@admin.com`
  - Contraseña: `admin`
- **Init Script:** Se ejecuta automáticamente `init.sql` creando tablas, tipos ENUM y datos de ejemplo.

**Verificar que esté listo:**
```bash
docker-compose logs -f postgres | grep "database system is ready"
```

### 2️⃣ Ejecutar la Aplicación
```bash
cd gestion-cliente
./mvnw spring-boot:run
```

La aplicación se abrirá automáticamente en una ventana JavaFX.

**Accesos disponibles:**
- **Aplicación:** Ventana JavaFX (interfaz de escritorio)
- **pgAdmin:** [http://localhost:5050](http://localhost:5050)
- **PostgreSQL:** `localhost:5433` (desde herramientas locales como DBeaver)

---

## 🧪 Testing y Calidad

El proyecto implementa políticas de calidad estrictas validadas automáticamente en CI/CD:

✅ **Cobertura mínima:** 80% (líneas y ramas)  
✅ **Tests de integración:** Testcontainers con PostgreSQL real  
✅ **Build falla** si no se cumplen métricas

### Ejecutar Tests Localmente
```bash
# Suite completa (unitarios + integración)
./mvnw verify

# Solo tests unitarios (más rápido)
./mvnw test

# Ver reporte de cobertura
open target/site/jacoco/index.html       # macOS
xdg-open target/site/jacoco/index.html   # Linux
start target/site/jacoco/index.html      # Windows
```

### Estructura de Tests
```text
src/test/
├── java/
│   ├── unit/           # Tests aislados con mocks (Mockito)
│   │   ├── service/
│   │   └── controller/
│   └── integration/    # Tests con Testcontainers
│       └── repository/
└── resources/
    └── test-data.sql   # Fixtures para tests
```

---

## 🔄 Flujo de Trabajo (Gitflow)

### Estrategia de Ramas
```
main            ← Producción (protegida)
  ↑
develop         ← Desarrollo activo
  ↑
feature/JIRA-123-nombre    ← Nuevas funcionalidades
fix/bug-descripcion        ← Correcciones
```

### Conventional Commits

Todos los commits deben seguir este formato:
```bash
# Ejemplos válidos
git commit -m "feat(clientes): agregar validación de CUIT"
git commit -m "fix(ventas): corregir cálculo de cuotas"
git commit -m "refactor(repos): optimizar queries N+1"
git commit -m "test(integration): agregar casos para pagos parciales"
git commit -m "docs(readme): actualizar instrucciones de deploy"
git commit -m "chore(deps): actualizar Spring Boot a 3.5.1"
```

**Tipos de commits:**
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bugs
- `refactor`: Cambios sin afectar funcionalidad externa
- `test`: Agregar o modificar tests
- `docs`: Documentación
- `chore`: Tareas de mantenimiento (dependencias, configuraciones)
- `perf`: Mejoras de performance

### Workflow Completo
```bash
# 1. Crear rama desde develop
git checkout develop
git pull origin develop
git checkout -b feature/JIRA-123-gestion-autores

# 2. Desarrollar con commits atómicos
git add .
git commit -m "feat(autores): crear entidad y repositorio"

# 3. Push y crear Pull Request
git push origin feature/JIRA-123-gestion-autores

# 4. Esperar aprobación y merge automático a develop
```

**⚠️ Reglas de Protección:**
- `main`: Requiere PR aprobado + CI en verde
- `develop`: Requiere CI en verde
- Squash merge recomendado para mantener historial limpio

---

## 📊 CI/CD y Reportes

Los badges en el encabezado se actualizan automáticamente en cada push a `main`:

- **Build Status:** ✅ Pasa / ❌ Falla
- **Coverage:** Porcentaje de líneas cubiertas
- **Branches:** Porcentaje de ramas condicionales cubiertas

**Descargar reportes:** En cada ejecución de GitHub Actions, el reporte completo de JaCoCo está disponible en la sección **Artifacts** (retención: 30 días).

---

## 🐳 Docker Compose

### Servicios Incluidos
```yaml
services:
  postgres:
    # Puerto 5433 (host) → 5432 (contenedor)
    # Evita colisiones con instalaciones locales
    
  pgadmin:
    # Puerto 5050
    # Credenciales: admin@admin.com / admin
```

### Comandos Útiles
```bash
# Ver logs en tiempo real
docker-compose logs -f postgres

# Reiniciar servicios
docker-compose restart

# Detener servicios
docker-compose down

# Eliminar TODO (incluyendo volúmenes de datos)
docker-compose down -v

# Reconstruir imágenes
docker-compose up -d --build
```

### Conectar desde DBeaver/DataGrip
```
Host: localhost
Port: 5433
Database: libreria_db
User: admin_libros
Password: secure_password_123
```

---

## 📁 Estructura del Proyecto
```text
.
├── .github/
│   ├── badges/              # Badges de cobertura generados por CI
│   └── workflows/
│       └── maven-build.yml  # Pipeline de CI/CD
│
├── docker-compose.yml       # Orquestación de infraestructura
├── init.sql                 # Schema inicial (futuro: Flyway)
│
└── gestion-cliente/         # Módulo principal Maven
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   │   └── com/libros/gestion_cliente/
    │   │   └── resources/
    │   │       ├── application.yml
    │   │       ├── fxml/         # Vistas JavaFX
    │   │       └── css/          # Estilos AtlantaFX
    │   └── test/
    │       ├── unit/
    │       └── integration/
    └── pom.xml
```

---

## 🚧 Roadmap

### En Desarrollo
- [ ] Migración a Flyway para versionado de schema
- [ ] Autenticación y control de acceso (Spring Security)
- [ ] Módulo de reportes avanzados (Dashboard con gráficos)

### Backlog
- [ ] Sincronización multi-dispositivo (Offline-First con sincronización eventual)
- [ ] App móvil complementaria (React Native)
- [ ] Integración con proveedores de libros (APIs externas)

---

## 📞 Soporte

Para reportar bugs o solicitar nuevas funcionalidades:

1. Verificar que no exista un issue similar en [Issues](https://github.com/Cammisi/SistemaGestionLibros/issues)
2. Crear un nuevo issue usando las plantillas provistas
3. Para contribuciones, leer [CONTRIBUTING.md](CONTRIBUTING.md) 


