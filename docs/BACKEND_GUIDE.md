# 🚛 VibeRoute Colombia - Guía para Desarrolladores (Backend)

Bienvenido compañero. Este documento resume los cambios realizados y la arquitectura del sistema para que puedas continuar con el desarrollo de forma fluida.

## 🚀 Logros y Cambios Clave

### 1. Motor de Ruteo de Precisión
Se reemplazó el trazado de líneas rectas por un sistema de **Street-Level Navigation** integrando la `Directions API` de Google Maps. 
- El `GoogleMapsAdapter` ahora maneja waypoints (paradas) y devuelve polilíneas codificadas que siguen las calles reales.
- El sistema tiene un **blindado de límites**: si un lote supera los 23 pedidos, el backend recorta automáticamente los puntos enviados a Google para evitar errores de cuota (`MAX_WAYPOINTS_EXCEEDED`), manteniendo la estabilidad del servidor.

### 2. Nuevo Sistema de Novedades (Incidentes)
Se eliminó el estado genérico `NOVEDAD` del enum `OrderStatus` para evitar confusiones.
- **Flujo Actual:** Cuando un conductor reporta un problema, debe elegir un estado final (`CANCELLED` o `RETURNED`).
- **Motivo Obligatorio:** Se añadió el campo `nonDeliveryReason` a la entidad `Order`. El conductor debe ingresar un texto explicando el incidente, el cual se guarda y se envía vía WebSocket al Muro de Eventos de Logística al instante.

### 3. Aislamiento Regional (Ciudad)
Se implementó lógica de filtrado por ciudad (`Pasto`, `Bogotá`, etc.) en:
- `OrderRepository`: Consultas nativas para promedios financieros y listados de pedidos pendientes.
- `BatchService`: Los lotes se crean segmentados por ciudad para evitar que un conductor de Pasto vea pedidos de Bogotá. Si el administrador no selecciona ciudad, el sistema opera en modo "Global".

### 4. Inteligencia Artificial (Gemini)
Integración con `AIRouteSuggestionService` para analizar el tráfico y la prioridad de los pedidos antes de que el conductor inicie la ruta.

---

## 🛠️ Stack Tecnológico Backend
- **Java 22** + **Spring Boot 3.2.3**
- **Postgres + PostGIS**: Para cálculos geoespaciales (`ST_DWithin`).
- **Jackson**: Configurado para manejar serialización de Enums y tipos de fecha locales.
- **STOMP/WebSockets**: Para notificaciones push a Logística.

## 📦 Docker y despliegue
El proyecto usa `docker-compose.yml` para orquestar:
1. `viberoute-db`: PostGIS en el puerto `5433` (mapeado desde el 5432 interno).
2. `viberoute-backend`: App Spring Boot expuesta en el puerto `8080`.
3. `viberoute-frontend`: App React expuesta en el puerto `3000`.

**Comando de reinicio limpio:**
```powershell
docker-compose down -v ; docker-compose up -d --build
```
*(Nota: El `-v` borra los datos, ideal para pruebas de pre-carga limpia).*
