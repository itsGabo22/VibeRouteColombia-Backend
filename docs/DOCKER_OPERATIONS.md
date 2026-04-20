# 🐳 VibeRoute Colombia - Guía de Operaciones Docker

El sistema está totalmente contenedorizado para asegurar consistencia entre entornos.

## 🏗️ Composición del Cluster
El archivo `docker-compose.yml` define 3 servicios esenciales que se comunican en una red virtual privada:

1.  **`viberoute-db`**: Imagen `postgis/postgis:15-3.3` (Puerto 5433).
2.  **`viberoute-backend`**: Construcción multi-etapa de Maven para generar un JAR ligero sobre OpenJDK 22.
3.  **`viberoute-frontend`**: Construcción multi-etapa de Node.js que genera archivos estáticos servidos por Nginx.

## 🛠️ Comandos de Mantenimiento

### Despliegue desde cero (Recomendado para cambios en DB)
```powershell
docker-compose down -v ; docker-compose up -d --build
```
*Este comando borra el volumen persistente de la base de datos, recrea el esquema y carga los datos de prueba (`UserSeeder`).*

### Ver logs en tiempo real
- **Todo el sistema:** `docker-compose logs -f`
- **Solo Backend:** `docker-compose logs -f backend`
- **Solo Frontend:** `docker-compose logs -f frontend`

### Limpieza de contenedores huérfanos
Si encuentras errores de puertos ya ocupados:
```powershell
docker stop $(docker ps -aq) ; docker rm $(docker ps -aq)
```

## 🌐 Endpoints
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080/api/v1
- **API Docs (Swagger):** http://localhost:8080/swagger-ui.html
