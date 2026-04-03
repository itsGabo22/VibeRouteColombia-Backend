# 🚀 VibeRoute Colombia — Backend
**Sistema Inteligente de Logística y Optimización de Rutas.**
Este repositorio contiene el "Cerebro" de VibeRoute: una API robusta construida en Java 17 que integra algoritmos matemáticos de optimización, inteligencia artificial generativa y servicios geoespaciales críticos.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) 
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![PostGIS](https://img.shields.io/badge/PostGIS-00304D?style=for-the-badge&logo=postgis&logoColor=white)
![Google Gemini](https://img.shields.io/badge/Google_Gemini-4285F4?style=for-the-badge&logo=google-gemini&logoColor=white)

---

## 🏗️ Arquitectura: Layered / Service-Oriented
El backend está diseñado bajo un modelo de capas que garantiza el desacoplamiento de la lógica de negocio frente a la infraestructura y la persistencia:

*   **`controller/`**: Endpoints RESTful con seguridad JWT y control de acceso por roles.
*   **`service/`**: Orquestación de lógica de dominio (Gestión de Batching, Asignación de Roles).
*   **`optimization/`**: El motor de cálculo. Implementa estrategias de resolución para el Problema del Viajante (TSP).
*   **`external/`**: Adaptadores para servicios de terceros (Google Maps Distance Matrix, Gemini AI).
*   **`model/` & `repository/`**: Capa de persistencia JPA con soporte nativo para funciones espaciales (PostGIS).

---

## 🧬 Ingeniería: Patrones y Estructuras (Rigor Académico)
Este proyecto implementa conceptos avanzados de ingeniería de software para resolver problemas logísticos reales:

### 1. Patrones de Diseño (Los 7 Pilares)
1.  **Strategy**: Intercambio dinámico de algoritmos de optimización (ej. OR-Tools vs Greedy).
2.  **Adapter**: Abstracción de APIs externas (Maps, Gemini) para evitar dependencia directa del código.
3.  **Builder**: Construcción segura de entidades complejas como `Route` y `Batch`.
4.  **Repository**: Abstracción de la persistencia de datos (Spring Data JPA).
5.  **DTO (Data Transfer Object)**: Seguridad en la transferencia de datos entre API y Cliente.
6.  **Dependency Injection (DI)**: Inyección automática de componentes vía Spring IoC.
7.  **Observer/WebSockets**: Notificación reactiva a conductores cuando se les asigna un lote.

### 2. Estructuras de Datos Aplicadas
*   **Pilas (Stacks - LIFO)**: Utilizadas por el motor de Backtracking de OR-Tools para explorar el árbol de rutas posibles y hallar la óptima.
*   **Colas (Queues - FIFO)**: Gestión de pedidos entrantes (Primer pedido en llegar, primero en ser asignado a un Lote).
*   **Matrices (Arrays 2D)**: Base fundamental para la "Matriz de Distancias" que cruza todos los puntos de entrega antes de la optimización.

---

## 🔗 Configuración de Entorno
El backend requiere la configuración de variables sensibles para operar con los servicios de Google y la base de datos.

### Variables Requeridas (`.env` o System Variables)
Defina las siguientes variables en su entorno de ejecución:

```bash
# SEGURIDAD
JWT_SECRET=tu_llave_secreta_super_segura

# BASE DE DATOS
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password

# APIs EXTERNAS (GOOGLE CLOUD)
GOOGLE_MAPS_KEY=tu_api_key_de_maps
GOOGLE_GEMINI_KEY=tu_api_key_de_gemini
```

> [!IMPORTANT]
> El sistema utiliza **PostGIS**. Asegúrese de que su instancia de PostgreSQL tenga instalada la extensión geográfica para que las alertas de colisión funcionen.

---

## 🛠️ Comandos de Desarrollo
Este proyecto utiliza **Maven** para la gestión de dependencias y construcción.

**Compilar y probar:**
```bash
mvn clean test
```

**Ejecutar localmente:**
```bash
mvn spring-boot:run
```

**Despliegue con Docker (Recomendado):**
```bash
docker-compose up -d --build
```

---

## 📄 Notas Adicionales
*   **Seguridad**: Implementación de JWT (JSON Web Token) con expiración y roles (ADMIN, LOGISTICS, DRIVER).
*   **Geospatial**: Detección de cercanía (Geofencing) automática mediante queries espaciales en la base de datos.
*   **Inteligencia Artificial**: Reportes automáticos diarios generados por el modelo `gemini-2.5-flash`.
